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
import nl.b3p.brmo.bag2.xml.leveringsdocument.Gemeente;
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.HttpClientWrapper;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import nl.b3p.brmo.util.http.wrapper.Java11HttpClientWrapper;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
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
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

@Command(name = "bag2-loader", mixinStandardHelpOptions = true, versionProvider = BAG2LoaderMain.class,
    resourceBundle = BAG2LoaderUtils.BUNDLE_NAME, subcommands = {BAG2MutatiesCommand.class})
public class BAG2LoaderMain implements IVersionProvider {
    private static Log log;

    /* zodat we een JNDI database kunnen gebruiken */
    private BAG2Database bag2Database = null;

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

    @Override
    public String[] getVersion() {
        return new String[] {
                BAG2LoaderUtils.getLoaderVersion(),
                BAG2LoaderUtils.getUserAgent()
        };
    }

    @Command(name = "load", sortOptions = false)
    public int load(
            @Mixin BAG2DatabaseOptions dbOptions,
            @Mixin BAG2LoadOptions loadOptions,
            @Mixin BAG2ProgressOptions progressOptions,
            @Parameters(paramLabel = "<file>") String[] filenames,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        log.info(BAG2LoaderUtils.getUserAgent());

        try(BAG2Database db = getBAG2Database(dbOptions)) {
            BAG2ProgressReporter progressReporter = progressOptions.isConsoleProgressEnabled()
                    ? new BAG2ConsoleProgressReporter()
                    : new BAG2ProgressReporter();

            loadFiles(db, dbOptions, loadOptions, progressReporter, filenames, null);
            return ExitCode.OK;
        }
    }

    private BAG2Database getBAG2Database(BAG2DatabaseOptions dbOptions) throws ClassNotFoundException {
        if (bag2Database == null) {
            bag2Database = new BAG2Database(dbOptions);
        }
        return bag2Database;
    }

    public BAG2Database getBag2Database() {
        return bag2Database;
    }

    public void setBag2Database(BAG2Database bag2Database) {
        this.bag2Database = bag2Database;
    }

    public void loadFiles(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames, CookieManager cookieManager) throws Exception {

        if (filenames.length == 1 && Files.isDirectory(Path.of(filenames[0]))) {
            log.info("Directory opgegeven, kijken naar toepasbare mutaties...");
            filenames = Files.list(Path.of(filenames[0]))
                    .filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".zip"))
                    .map(p -> p.toAbsolutePath().toString())
                    .toArray(String[]::new);
            if (filenames.length == 0) {
                log.info("Geen ZIP bestanden gevonden, niets te doen");
            } else {
                applyMutaties(db, dbOptions, loadOptions, progressReporter, filenames, null,null);
            }
            return;
        }

        BAG2LoaderUtils.BAGExtractLeveringWrapper bagExtractLevering = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(filenames[0]);

        if (bagExtractLevering.isStand()) {
            if (bagExtractLevering.isGebiedNLD()) {
                if (filenames.length > 1) {
                    throw new IllegalArgumentException("Inladen stand heel Nederland: teveel bestanden opgegeven");
                }
            } else {
                // Verify all filenames are gemeentestanden
                Set<String> gemeenteCodes = new HashSet<>();
                for (int i = 1; i < filenames.length; i++) {
                    BAG2LoaderUtils.BAGExtractLeveringWrapper nextBagExtractLevering = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(filenames[i]);

                    if (!nextBagExtractLevering.isStand() || !nextBagExtractLevering.isGemeente()) {
                        throw new IllegalArgumentException("Inladen stand gemeentes, ongeldig bestand opgegeven (geen gemeentestand): " + filenames[i]);
                    }
                    Set<String> nextBagExtractLeveringGemeenteCodes = nextBagExtractLevering.getGebiedRegistratief()
                            .getGebiedGEM().getGemeenteCollectie().getGemeente().stream()
                            .map(Gemeente::getGemeenteIdentificatie)
                            .collect(Collectors.toSet());

                    if (gemeenteCodes.stream().anyMatch(nextBagExtractLeveringGemeenteCodes::contains)) {
                        throw new IllegalArgumentException("Inladen stand gemeentes, dubbele gemeentecode in bestand: " + filenames[i]);
                    }
                    gemeenteCodes.addAll(nextBagExtractLeveringGemeenteCodes);
                }
            }

            loadStandFiles(db, dbOptions, loadOptions, progressReporter, filenames, cookieManager);
        } else {
            // Process mutaties while ignoring files not applicable
            applyMutaties(db, dbOptions, loadOptions, progressReporter, filenames, null,null);
        }
    }

    /**
     * Only called after list of files have been checked to only have been entire NL stand or unique gemeente standen.
     */
    private void loadStandFiles(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames, CookieManager cookieManager) throws Exception {
        try {
            // When loading multiple standen (for gemeentes), set ignore duplicates so the seen object keys are kept in
            // memory so duplicates can be ignored. Don't keep keys in memory for entire NL stand.
            loadOptions.setIgnoreDuplicates(filenames.length > 1);

            BAG2GMLMutatieGroepStream.BagInfo bagInfo = null;
            String lastFilename = null;

            // Keep track of which gemeentes are loaded so the correct mutations can be processed
            Set<String> gemeenteIdentificaties = new HashSet<>();

            for (String filename: filenames) {
                BAG2GMLMutatieGroepStream.BagInfo latestBagInfo = loadBAG2ExtractFromURLorFile(db, loadOptions, dbOptions, progressReporter, filename);
                if (bagInfo != null) {
                    // For gemeentes the BagInfo must be the same so the standen are of the same date
                    if (!latestBagInfo.equals(bagInfo)) {
                        throw new IllegalArgumentException(String.format("Incompatible BagInfo for file \"%s\" (%s) compared to last file \"%s\" (%s)",
                                filename,
                                latestBagInfo,
                                lastFilename,
                                bagInfo));
                    }
                }
                bagInfo = latestBagInfo;

                // For NL stand this will be "9999"
                gemeenteIdentificaties.add(bagInfo.getGemeenteIdentificatie());
                lastFilename = filename;
            }
            if (bagInfo != null) {
                // TODO: when loading gemeente without rare objects such as ligplaatsen/standplaatsen, table will not be created
                // and a future change with such an object will fail. Should create entire schema up-front instead of when first
                // encountering object type
                createKeysAndIndexes(db, loadOptions, dbOptions, progressReporter);

                updateMetadata(db, loadOptions, true, gemeenteIdentificaties, bagInfo.getStandTechnischeDatum());
            }

            db.getConnection().commit();
        } finally {
            progressReporter.reportTotalSummary();
        }
    }

    public void applyMutaties(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames, String[] urls, CookieManager cookieManager) throws Exception {
        if (filenames.length == 0) {
            return;
        }
        BAG2LoaderUtils.BAGExtractLeveringWrapper bagExtractLevering = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(filenames[0]);
        if (bagExtractLevering.isGemeente()) {
            applyGemeenteMutaties(db, dbOptions, loadOptions, progressReporter, filenames, urls, cookieManager);
        } else {
            applyNLMutaties(db, dbOptions, loadOptions, progressReporter, filenames, urls, cookieManager);
        }
    }

    private void applyGemeenteMutaties(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames, String[] urls, CookieManager cookieManager) throws Exception {
        LocalDate currentTechnischeDatum = db.getCurrentTechnischeDatum();
        Set<String> gemeenteCodes = db.getGemeenteCodes();

        Set<Integer> applicableMutatieIndexes;
        do {
            applicableMutatieIndexes = new HashSet<>();

            Set<String> missingGemeentes = new HashSet<>(gemeenteCodes);
            for(int i = 0; i < filenames.length; i++) {
                BAG2LoaderUtils.BAGExtractLeveringWrapper bagExtractLevering = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(filenames[i]);

                if (bagExtractLevering.isGemeente()
                        && bagExtractLevering.isMutaties()
                        && bagExtractLevering.getMutatiesFrom().equals(currentTechnischeDatum)
                        && gemeenteCodes.contains(bagExtractLevering.getGemeenteCodes())) {
                    applicableMutatieIndexes.add(i);
                    missingGemeentes.remove(bagExtractLevering.getGemeenteCodes());
                }
            }

            if (applicableMutatieIndexes.isEmpty()) {
                log.info(String.format("Geen nieuw toe te passen gemeentemutatiebestanden gevonden voor huidige stand technische datum %s, klaar", currentTechnischeDatum));
                break;
            }

            // Check whether applicable mutaties are available for all gemeentecodes because they need to be processed
            // at the same time to ignore duplicates
            if (!missingGemeentes.isEmpty()) {
                throw new IllegalArgumentException(String.format("Kan geen gemeente mutaties toepassen voor gemeentes %s vanaf stand technische datum %s, in opgegeven mutatiebestanden ontbreken gemeentecodes %s",
                        gemeenteCodes,
                        currentTechnischeDatum,
                        missingGemeentes));
            }

            log.info(String.format("Toepassen gemeentemutaties voor %d gemeentes vanaf stand technische datum %s...", gemeenteCodes.size(), currentTechnischeDatum));

            BAG2GMLMutatieGroepStream.BagInfo bagInfo = null;

            loadOptions.setIgnoreDuplicates(gemeenteCodes.size() > 1);
            for (int index: applicableMutatieIndexes) {
                String filename = filenames[index];
                String url = urls == null ? filenames[index] : urls[index];
                bagInfo = loadBAG2ExtractFromURLorFile(db, loadOptions, dbOptions, progressReporter, filename, url, cookieManager);
            }
            currentTechnischeDatum = new java.sql.Date(bagInfo.getStandTechnischeDatum().getTime()).toLocalDate();
            updateMetadata(db, loadOptions, false, null, bagInfo.getStandTechnischeDatum());
            db.getConnection().commit();
            // Duplicates need only be checked for mutaties for a single from date, clear cache to reduce memory usage
            clearDuplicatesCache();
            log.info("Mutaties verwerkt, huidige stand technische datum: " + currentTechnischeDatum);

        } while(true);
    }

    private void applyNLMutaties(BAG2Database db, BAG2DatabaseOptions dbOptions, BAG2LoadOptions loadOptions, BAG2ProgressReporter progressReporter, String[] filenames, String[] urls, CookieManager cookieManager) throws Exception {
        LocalDate currentTechnischeDatum = db.getCurrentTechnischeDatum();
        do {
            String applicableMutatie = null;
            String applicatieMutatieURL = null;

            for(int i = 0; i < filenames.length; i++) {
                String filename = filenames[i];
                BAG2LoaderUtils.BAGExtractLeveringWrapper bagExtractLeveringWrapper = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(filename);

                if (bagExtractLeveringWrapper.isGebiedNLD()
                        && bagExtractLeveringWrapper.isMutaties()
                        && bagExtractLeveringWrapper.getMutatiesFrom().equals(currentTechnischeDatum)) {
                    applicableMutatie = filename;
                    applicatieMutatieURL = urls == null ? filename : urls[i];
                }
            }

            if (applicableMutatie == null) {
                log.info(String.format("Geen nieuw toe te passen mutatiebestanden gevonden voor huidige stand technische datum %s, klaar", currentTechnischeDatum));
                break;
            }

            log.info(String.format("Toepassen mutaties vanaf stand technische datum %s...", currentTechnischeDatum));

            BAG2GMLMutatieGroepStream.BagInfo bagInfo = loadBAG2ExtractFromURLorFile(db, loadOptions, dbOptions, progressReporter, applicableMutatie, applicatieMutatieURL, cookieManager);
            currentTechnischeDatum = new java.sql.Date(bagInfo.getStandTechnischeDatum().getTime()).toLocalDate();
            updateMetadata(db, loadOptions, false, null, bagInfo.getStandTechnischeDatum());
            db.getConnection().commit();
            log.info("Mutaties verwerkt, huidige stand technische datum: " + currentTechnischeDatum);
        } while(true);
    }

    private void createKeysAndIndexes(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions databaseOptions, BAG2ProgressReporter progressReporter) throws Exception {
        BAG2ObjectTableWriter writer = db.createObjectTableWriter(loadOptions, databaseOptions);
        writer.setProgressUpdater(progressReporter);
        for(BAG2ObjectType objectType: objectTypesWithSchemaCreated) {
            writer.createKeys(objectType); // BAG2 writer is always a single ObjectType unlike BGT
            writer.createIndexes(objectType);
        }
    }

    private BAG2GMLMutatieGroepStream.BagInfo loadBAG2ExtractFromURLorFile(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions, BAG2ProgressReporter progressReporter, String filename) throws Exception {
        return loadBAG2ExtractFromURLorFile(db, loadOptions, dbOptions, progressReporter, filename, filename, null);
    }

    private BAG2GMLMutatieGroepStream.BagInfo loadBAG2ExtractFromURLorFile(BAG2Database db, BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions, BAG2ProgressReporter progressReporter, String filename, String url, CookieManager cookieManager) throws Exception {
        HttpClientWrapper<HttpRequest.Builder, HttpResponse<InputStream>> httpClientWrapper = cookieManager == null
                ? new Java11HttpClientWrapper()
                : new Java11HttpClientWrapper(HttpClient.newBuilder().cookieHandler(cookieManager));

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try (InputStream in = new ResumingInputStream(new HttpStartRangeInputStreamProvider(URI.create(url), httpClientWrapper))) {
                return loadBAG2ExtractFromStream(db, loadOptions, dbOptions, progressReporter, filename, in);
            }
        }
        if (url.endsWith(".zip")) {
            try (InputStream in = new FileInputStream(url)) {
                return loadBAG2ExtractFromStream(db, loadOptions, dbOptions, progressReporter, filename, in);
            }
        }

        throw new IllegalArgumentException(getMessageFormattedString("load.invalid_file", url));
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

    private void clearDuplicatesCache() {
        keysPerObjectType = new HashMap<>();
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
}
