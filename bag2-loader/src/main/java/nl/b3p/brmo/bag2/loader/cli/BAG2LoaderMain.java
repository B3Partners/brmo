/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import nl.b3p.brmo.bag2.loader.BAG2Database;
import nl.b3p.brmo.bag2.loader.BAG2LoaderUtils;
import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.bag2.schema.BAG2ObjectType;
import nl.b3p.brmo.bag2.schema.BAG2Schema;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.util.logging.Logging;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;

@Command(name = "bag2-loader", mixinStandardHelpOptions = true, versionProvider = BAG2LoaderMain.class,
    resourceBundle = BAG2LoaderUtils.BUNDLE_NAME)
public class BAG2LoaderMain implements IVersionProvider {
    private static Log log;

    private Set<BAG2ObjectType> objectTypesWithSchemaCreated = new HashSet<>();

    private Map<BAG2ObjectType, Set<Pair<Object,Object>>> keysPerObjectType = new HashMap<>();

    /**
     * init logging.
     *
     * @param standAlone set to {@code false} when using in a preconfigured environment, eg. calling methods from a servlet,
     *                   use {@code true} for commandline usage.
     */
    public static void configureLogging(boolean standAlone) {
        if (standAlone) {
            PropertyConfigurator.configure(BAG2LoaderMain.class.getResourceAsStream("/bag2-loader-cli-log4j.properties"));
            log = LogFactory.getLog(BAG2LoaderMain.class);
            try {
                Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            log = LogFactory.getLog(BAG2LoaderMain.class);
        }
    }

    public static void main(String... args) {
        configureLogging(true);

        CommandLine cmd = new CommandLine(new BAG2LoaderMain())
                .setUsageHelpAutoWidth(true);
        System.exit(cmd.execute(args));
    }

    @Command(name = "load", sortOptions = false)
    public int load(
            @Mixin BAG2DatabaseOptions dbOptions,
            @Mixin BAG2LoadOptions loadOptions,
            @Parameters(paramLabel = "<file>") String[] filenames,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        log.info(getUserAgent());

        Instant start = Instant.now();
        try(BAG2Database db = new BAG2Database(dbOptions)) {

            if (db.getDialect() instanceof PostGISDialect) {
                new QueryRunner().update(db.getConnection(), "create schema if not exists bag");
                new QueryRunner().update(db.getConnection(), "set search_path=bag,public");
            }

            // When loading multiple standen (for gemeentes), set ignore duplicates so the seen object keys are kept in
            // memory so duplicates can be ignored. Don't keep keys in memory for entire NL stand.
            loadOptions.setIgnoreDuplicates(filenames.length > 1);

            for(String filename: filenames) {
                if (filename.startsWith("http://") || filename.startsWith("https://")) {
                    try (InputStream in = new ResumingInputStream(new HttpStartRangeInputStreamProvider(new URI(filename)))) {
                        loadBAG2ExtractFromStream(db, loadOptions, dbOptions, filename, in);
                    }
                } else if (filename.endsWith(".zip")) {
                    try (InputStream in = new FileInputStream(filename)) {
                        loadBAG2ExtractFromStream(db, loadOptions, dbOptions, filename, in);
                    }
                } else {
                    throw new IllegalArgumentException(getMessageFormattedString("load.invalid_file", filename));
                }
            }
            completeAll(db, loadOptions, dbOptions);
        }
        System.out.println("Total time: " + formatTimeSince(start));
        return ExitCode.OK;
    }

    private void loadBAG2ExtractFromStream(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions, String name, InputStream input) throws Exception {
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(input)) {
            ZipArchiveEntry entry = zip.getNextZipEntry();
            while(entry != null) {
                if (entry.getName().matches("[0-9]{4}(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.xml")) {
                    // Load extracted zipfile
                    loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, name, zip, entry);
                    break;
                }

                if (entry.getName().matches("[0-9]{4}GEM[0-9]{8}\\.zip")) {
                    loadBAG2ExtractFromStream(db, loadOptions, dbOptions, name, zip);
                    return;
                }

                // Process single and double-nested ZIP files

                if (entry.getName().matches("[0-9]{4}(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.zip")
                || entry.getName().matches("[0-9]{4}MUT[0-9]{8}-[0-9]{8}\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, entry.getName(), nestedZip, nestedZip.getNextZipEntry());
                }

                if (entry.getName().matches("[0-9]{4}Inactief.*\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    ZipArchiveEntry nestedEntry = nestedZip.getNextZipEntry();
                    while(nestedEntry != null) {
                        if (nestedEntry.getName().matches("[0-9]{4}IA.*\\.zip")) {
                            ZipArchiveInputStream moreNestedZip = new ZipArchiveInputStream(nestedZip);
                            loadXmlEntriesFromZipFile(db,  loadOptions, dbOptions,nestedEntry.getName(), moreNestedZip, moreNestedZip.getNextZipEntry());
                        }
                        nestedEntry = nestedZip.getNextZipEntry();
                    }
                }

                try {
                    entry = zip.getNextZipEntry();
                } catch(IOException e) {
                    // Reading the ZIP from HTTP may give this error, but it is a normal end...
                    if ("Truncated ZIP file".equals(e.getMessage())) {
                        break;
                    }
                }
            }

        }
    }

    private static BAG2ObjectType getObjectTypeFromFilename(String filename) {
        Matcher m = Pattern.compile(".*[0-9]{4}(IA)?(MUT|STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.(xml|zip)").matcher(filename);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid BAG2 filename: " + filename);
        }
        String objectTypeName = null;
        switch(m.group(2)) {
            case "MUT": break;
            case "STA": objectTypeName = "Standplaats"; break;
            case "OPR": objectTypeName = "OpenbareRuimte"; break;
            case "VBO": objectTypeName = "Verblijfsobject"; break;
            case "NUM": objectTypeName = "Nummeraanduiding"; break;
            case "LIG": objectTypeName = "Ligplaats"; break;
            case "PND": objectTypeName = "Pand"; break;
            case "WPL": objectTypeName = "Woonplaats"; break;
        }
        if (objectTypeName == null) {
            return null;
        } else {
            return BAG2Schema.getInstance().getObjectTypeByName(objectTypeName);
        }
    }

    private void completeAll(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions) throws Exception {
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        for(BAG2ObjectType objectType: objectTypesWithSchemaCreated) {
            Instant start = Instant.now();
            System.out.print("\r" + objectType.getName() + ": creating keys, indexes and views...");
            writer.createKeys(objectType); // BAG2 writer is always a single ObjectType unlike BGT
            writer.createIndexes(objectType);
            writer.getConnection().commit();
            System.out.println(" " + formatTimeSince(start));
        }
    }

    private void loadXmlEntriesFromZipFile(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions, String name, ZipArchiveInputStream zip, ZipArchiveEntry entry) throws Exception {
        BAG2ObjectType objectType = getObjectTypeFromFilename(name);
        // objectType is null for mutaties, which contain mixed object types instead of a single object type with stand
        boolean schemaCreated = objectType == null || objectTypesWithSchemaCreated.contains(objectType);
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        writer.setCreateSchema(!schemaCreated);
        writer.setCreateKeysAndIndexes(false);
        writer.setKeysPerObjectType(keysPerObjectType);
        writer.start(); // sets InitialLoad to true
        writer.getProgress().setInitialLoad(!schemaCreated); // For a COPY in transaction, table must be created or truncated in it
        if (objectType == null) {
            // When processing mutaties, set batch size to 1 so all mutaties are processed sequentially and can not
            // conflict with deleting and inserting of old/new versions
            writer.setBatchSize(1);
            // Disable multithreading so deletion of previous versions and new inserts are processed sequentially
            writer.setMultithreading(false);
        }
        try {
            int files = 0;
            Instant start = Instant.now();

            while(entry != null) {
                System.out.print("\r" + name + ": " + entry.getName());
                writer.write(CloseShieldInputStream.wrap(zip));
                files++;
                if (loadOptions.getMaxObjects() != null && writer.getProgress().getObjectCount() == loadOptions.getMaxObjects()) {
                    break;
                }
                entry = zip.getNextZipEntry();
            }
            writer.complete();

            if (writer.getProgress().getObjectCount() > 0 && objectType != null) {
                objectTypesWithSchemaCreated.add(objectType);
            }

            System.out.printf("\r%s: loaded %,d files, %,d total objects in %s\n", name, files, writer.getProgress().getObjectCount(), formatTimeSince(start));
        } catch(Exception e) {
            writer.abortWorkerThread();
            throw e;
        }
    }

    @Override
    public String[] getVersion() {
        return new String[] {
                BAG2LoaderUtils.getLoaderVersion(),
                BAG2LoaderUtils.getUserAgent()
        };
    }
}
