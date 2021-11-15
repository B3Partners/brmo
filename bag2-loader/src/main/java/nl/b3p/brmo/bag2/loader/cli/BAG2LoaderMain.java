/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import nl.b3p.brmo.bag2.loader.BAG2Database;
import nl.b3p.brmo.bag2.loader.BAG2GMLMutatieGroepStream;
import nl.b3p.brmo.bag2.loader.BAG2LoaderUtils;
import nl.b3p.brmo.bag2.loader.BAG2ProgressReporter;
import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.bag2.schema.BAG2ObjectType;
import nl.b3p.brmo.bag2.schema.BAG2Schema;
import nl.b3p.brmo.bag2.schema.BAG2SchemaMapper;
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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.METADATA_TABLE_NAME;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.Metadata.CURRENT_TECHNISCHE_DATUM;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.Metadata.FILTER_MUTATIES_WOONPLAATS;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.Metadata.GEMEENTE_CODES;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.Metadata.STAND_LOAD_TECHNISCHE_DATUM;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.Metadata.STAND_LOAD_TIME;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

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
            @Mixin BAG2ProgressOptions progressOptions,
            @Parameters(paramLabel = "<file>") String[] filenames,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        log.info(BAG2LoaderUtils.getUserAgent());

        try(BAG2Database db = new BAG2Database(dbOptions)) {

            BAG2ProgressReporter progressReporter = progressOptions.isConsoleProgressEnabled()
                    ? new BAG2ConsoleProgressReporter()
                    : new BAG2ProgressReporter();

            loadFiles(db, dbOptions, loadOptions, progressReporter, filenames);
            return ExitCode.OK;
        }
    }

    public void loadFiles(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames) throws Exception {
        try {
            if (db.getDialect() instanceof PostGISDialect) {
                new QueryRunner().update(db.getConnection(), "create schema if not exists bag");
                new QueryRunner().update(db.getConnection(), "set search_path=bag,public");
            }

            // When loading multiple standen (for gemeentes), set ignore duplicates so the seen object keys are kept in
            // memory so duplicates can be ignored. Don't keep keys in memory for entire NL stand.
            loadOptions.setIgnoreDuplicates(filenames.length > 1);

            BAG2GMLMutatieGroepStream.BagInfo bagInfo = null;
            String lastFilename = null;

            // Keep track of which gemeentes are loaded so the correct mutations can be processed
            Set<String> gemeenteIdentificaties = new HashSet<>();
            boolean stand;

            for (String filename: filenames) {
                BAG2GMLMutatieGroepStream.BagInfo latestBagInfo;
                if (filename.startsWith("http://") || filename.startsWith("https://")) {
                    try (InputStream in = new ResumingInputStream(new HttpStartRangeInputStreamProvider(new URI(filename)))) {
                        latestBagInfo = loadBAG2ExtractFromStream(db, loadOptions, dbOptions, progressReporter, filename, in);
                    }
                } else if (filename.endsWith(".zip")) {
                    try (InputStream in = new FileInputStream(filename)) {
                        latestBagInfo = loadBAG2ExtractFromStream(db, loadOptions, dbOptions, progressReporter, filename, in);
                    }
                } else {
                    throw new IllegalArgumentException(getMessageFormattedString("load.invalid_file", filename));
                }
                if (bagInfo != null) {
                    if (!latestBagInfo.equals(bagInfo)) {
                        throw new IllegalArgumentException(String.format("Incompatible BagInfo for file \"%s\" (%s) compared to last file \"%s\" (%s)",
                                filename,
                                latestBagInfo,
                                lastFilename,
                                bagInfo));
                    }
                }
                bagInfo = latestBagInfo;
                stand = bagInfo.getMutatieDatumVanaf() == null;
                if (stand) {
                    gemeenteIdentificaties.add(bagInfo.getGemeenteIdentificatie());
                }
                lastFilename = filename;
            }
            if (bagInfo != null) {
                completeAll(db, loadOptions, dbOptions, progressReporter);

                updateMetadata(db, loadOptions, bagInfo.getMutatieDatumVanaf() == null, gemeenteIdentificaties, bagInfo.getStandTechnischeDatum());
            }

            db.getConnection().commit();
        } finally {
            progressReporter.reportTotalSummary();
        }
    }

    private BAG2GMLMutatieGroepStream.BagInfo loadBAG2ExtractFromStream(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions, BAG2ProgressReporter progressReporter, String name, InputStream input) throws Exception {
        BAG2GMLMutatieGroepStream.BagInfo bagInfo = null;
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(input)) {
            ZipArchiveEntry entry = zip.getNextZipEntry();
            while(entry != null) {
                if (entry.getName().matches("[0-9]{4}(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.xml")) {
                    // Load extracted zipfile
                    bagInfo = loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, progressReporter, name, zip, entry);
                    break;
                }

                if (entry.getName().matches("[0-9]{4}GEM[0-9]{8}\\.zip")) {
                    return loadBAG2ExtractFromStream(db, loadOptions, dbOptions, progressReporter, name, zip);
                }

                // Process single and double-nested ZIP files

                if (entry.getName().matches("[0-9]{4}(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.zip")
                || entry.getName().matches("[0-9]{4}MUT[0-9]{8}-[0-9]{8}\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    bagInfo = loadXmlEntriesFromZipFile(db, loadOptions, dbOptions, progressReporter, entry.getName(), nestedZip, nestedZip.getNextZipEntry());
                }

                if (entry.getName().matches("[0-9]{4}Inactief.*\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    ZipArchiveEntry nestedEntry = nestedZip.getNextZipEntry();
                    while(nestedEntry != null) {
                        if (nestedEntry.getName().matches("[0-9]{4}IA.*\\.zip")) {
                            ZipArchiveInputStream moreNestedZip = new ZipArchiveInputStream(nestedZip);
                            bagInfo = loadXmlEntriesFromZipFile(db,  loadOptions, dbOptions, progressReporter, nestedEntry.getName(), moreNestedZip, moreNestedZip.getNextZipEntry());
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
        return bagInfo;
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

    private void completeAll(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions, BAG2ProgressReporter progressReporter) throws Exception {
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        writer.setProgressUpdater(progressReporter);
        for(BAG2ObjectType objectType: objectTypesWithSchemaCreated) {
            writer.createKeys(objectType); // BAG2 writer is always a single ObjectType unlike BGT
            writer.createIndexes(objectType);
        }
    }

    private void updateMetadata(BAG2Database db, BAG2LoadOptions loadOptions, boolean stand, Set<String> gemeenteIdentificaties, Date standTechnischeDatum) throws Exception {

        // Check if metadata table already exists. For PostgreSQL we can use the metadata table in the public schema
        if (!db.getDialect().tableExists(db.getConnection(), METADATA_TABLE_NAME)) {
            // Create a new metadata table, for Oracle as BAG is in separate schema, for PostgreSQL if loading BAG
            // into a non-brmo RSGB database
            db.createMetadataTable(loadOptions);
        }

        db.setMetadataValue(BAG2SchemaMapper.Metadata.LOADER_VERSION, BAG2LoaderUtils.getLoaderVersion());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (stand) {
            db.setMetadataValue(STAND_LOAD_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            db.setMetadataValue(STAND_LOAD_TECHNISCHE_DATUM, df.format(standTechnischeDatum));
            db.setMetadataValue(GEMEENTE_CODES, String.join(",", gemeenteIdentificaties));
            db.setMetadataValue(FILTER_MUTATIES_WOONPLAATS, "false");
        }
        db.setMetadataValue(CURRENT_TECHNISCHE_DATUM, df.format(standTechnischeDatum));
    }

    private BAG2GMLMutatieGroepStream.BagInfo loadXmlEntriesFromZipFile(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions, BAG2ProgressReporter progressReporter, String name, ZipArchiveInputStream zip, ZipArchiveEntry entry) throws Exception {
        BAG2ObjectType objectType = getObjectTypeFromFilename(name);
        // objectType is null for mutaties, which contain mixed object types instead of a single object type with stand
        boolean schemaCreated = objectType == null || objectTypesWithSchemaCreated.contains(objectType);
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        writer.setProgressUpdater(progressReporter);
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
        progressReporter.startNewFile(name);
        try {
            while(entry != null) {
                progressReporter.startNextSplitFile(entry.getName());
                writer.write(CloseShieldInputStream.wrap(zip));
                if (loadOptions.getMaxObjects() != null && writer.getProgress().getObjectCount() == loadOptions.getMaxObjects()) {
                    break;
                }
                entry = zip.getNextZipEntry();
            }
            writer.complete();

            if (writer.getProgress().getObjectCount() > 0 && objectType != null) {
                objectTypesWithSchemaCreated.add(objectType);
            }

            return writer.getProgress().getMutatieInfo();
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
