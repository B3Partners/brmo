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
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
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
import java.util.HashSet;
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

    public static void main(String... args) throws Exception {
        configureLogging(true);

        CommandLine cmd = new CommandLine(new BAG2LoaderMain())
                .setUsageHelpAutoWidth(true);
        System.exit(cmd.execute(args));
    }

    @Command(name = "load", sortOptions = false)
    public int load(
            @Mixin BAG2DatabaseOptions dbOptions,
            @Mixin BAG2LoadOptions loadOptions,
            @Parameters(paramLabel = "<file>") String filename,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        log.info(getUserAgent());

        try(BAG2Database db = new BAG2Database(dbOptions)) {
            if (filename.startsWith("http://") || filename.startsWith("https://")) {
                loadBAG2ExtractFromStream(db, loadOptions, dbOptions, filename, new ResumingInputStream(new HttpStartRangeInputStreamProvider(new URI(filename))));
            } else if(filename.endsWith(".zip")) {
                loadBAG2ExtractFromStream(db, loadOptions, dbOptions, filename, new FileInputStream(filename));
            } else {
                throw new IllegalArgumentException(getMessageFormattedString("load.invalid_file", filename));
            }
        }
        return ExitCode.OK;
    }

    private void loadBAG2ExtractFromStream(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions, String name, InputStream input) throws Exception {
        Instant start = Instant.now();

        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(input)) {
            ZipArchiveEntry entry = zip.getNextZipEntry();
            while(entry != null) {
                if (entry.getName().matches("9999(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.xml")) {
                    // Load extracted zipfile
                    loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, name, zip, entry, true);
                    break;
                }

                // Process single and double-nested ZIP files

                if (entry.getName().matches("9999(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, entry.getName(), nestedZip, nestedZip.getNextZipEntry(), true);
                }

                if (entry.getName().startsWith("9999Inactief")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    ZipArchiveEntry nestedEntry = nestedZip.getNextZipEntry();
                    while(nestedEntry != null) {
                        if (nestedEntry.getName().startsWith("9999IA")) {
                            ZipArchiveInputStream moreNestedZip = new ZipArchiveInputStream(nestedZip);
                            loadXmlEntriesFromZipFile(db,  loadOptions, dbOptions,nestedEntry.getName(), moreNestedZip, moreNestedZip.getNextZipEntry(), false);
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
            completeAll(db, loadOptions, dbOptions);
        }
        System.out.println("Total time: " + formatTimeSince(start));
    }

    private static BAG2ObjectType getObjectTypeFromFilename(String filename) {
        Matcher m = Pattern.compile(".*9999(IA)?(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.(xml|zip)").matcher(filename);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid BAG2 filename: " + filename);
        }
        String objectTypeName = null;
        switch(m.group(2)) {
            case "STA": objectTypeName = "Standplaats"; break;
            case "OPR": objectTypeName = "OpenbareRuimte"; break;
            case "VBO": objectTypeName = "Verblijfsobject"; break;
            case "NUM": objectTypeName = "Nummeraanduiding"; break;
            case "LIG": objectTypeName = "Ligplaats"; break;
            case "PND": objectTypeName = "Pand"; break;
            case "WPL": objectTypeName = "Woonplaats"; break;
        }
        return BAG2Schema.getInstance().getObjectTypeByName(objectTypeName);
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

    private void loadXmlEntriesFromZipFile(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions, String name, ZipArchiveInputStream zip, ZipArchiveEntry entry, boolean createSchema) throws Exception {
        BAG2ObjectType objectType = getObjectTypeFromFilename(name);
        boolean schemaCreated = objectTypesWithSchemaCreated.contains(objectType);
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        writer.setCreateSchema(!schemaCreated);
        writer.setCreateKeysAndIndexes(false);
        writer.start(); // sets InitialLoad to true
        writer.getProgress().setInitialLoad(!schemaCreated); // For a COPY in transaction, table must be created or truncated in it

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

            if (writer.getProgress().getObjectCount() > 0) {
                objectTypesWithSchemaCreated.add(objectType);
            }

            System.out.printf("\r%s: loaded %,d files, %,d total objects in %s\n", name, files, writer.getProgress().getObjectCount(), formatTimeSince(start));
        } catch(Exception e) {
            writer.abortWorkerThread();
            throw e;
        }
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[] {
                BAG2LoaderUtils.getLoaderVersion(),
                BAG2LoaderUtils.getUserAgent()
        };
    }
}
