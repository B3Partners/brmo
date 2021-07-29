/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.BGTDatabase;
import nl.b3p.brmo.bgt.loader.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.loader.BGTSchemaMapper;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.bgt.loader.ResumableBGTDownloadInputStream;
import nl.b3p.brmo.bgt.loader.Utils;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CountingInputStream;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.Metadata;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;

@Command(name = "bgt-loader", mixinStandardHelpOptions = true, versionProvider = BGTLoaderMain.class,
        resourceBundle = Utils.BUNDLE_NAME, subcommands = {DownloadCommand.class})
public class BGTLoaderMain implements IVersionProvider {
    private static Log log;

    private static void configureLogging() {
        PropertyConfigurator.configure(BGTLoaderMain.class.getResourceAsStream("/bgt-loader-cli-log4.properties"));
        log = LogFactory.getLog(BGTLoaderMain.class);
        try {
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void main(String[] args) {
        configureLogging();
        CommandLine cmd = new CommandLine(new BGTLoaderMain())
                .setUsageHelpAutoWidth(true);
        System.exit(cmd.execute(args));
    }

    @Command(name = "schema", sortOptions = false)
    public int schema(
            @Option(names="--dialect", paramLabel="<dialect>", defaultValue = "postgis") BGTDatabase.SQLDialectEnum dialectEnum,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions,
            @Option(names="--table-prefix", defaultValue = "", hidden = true) String tablePrefix,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws SQLException {
        SQLDialect dialect = BGTDatabase.createDialect(dialectEnum);
        // For schema generation include plaatsbepalingspunt with 'all' and 'bgt'
        if (featureTypeSelectionOptions.featureTypes.contains("all") || featureTypeSelectionOptions.featureTypes.contains("bgt")) {
            featureTypeSelectionOptions.getFeatureTypes().add(DeltaCustomDownloadRequest.FeaturetypesEnum.PLAATSBEPALINGSPUNT.getValue());
        }
        Set<String> tableNames = featureTypeSelectionOptions.getFeatureTypesList().stream()
                .map(DeltaCustomDownloadRequest.FeaturetypesEnum::getValue)
                .collect(Collectors.toSet());
        BGTSchemaMapper.printSchema(dialect, tablePrefix, objectType ->
                tableNames.contains(BGTSchemaMapper.getTableNameForObjectType(objectType, ""))
        );
        return ExitCode.OK;
    }

    @Command(name = "load", sortOptions = false)
    public int load(
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions,
            @Parameters(paramLabel = "<file>") String file,
            @Mixin CLIOptions cliOptions,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        log.info(getUserAgent());

        try(BGTDatabase db = new BGTDatabase(dbOptions)) {
            BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);

            if (cliOptions.isConsoleProgressEnabled()) {
                writer.setProgressUpdater(new ConsoleProgressReporter());
            } else {
                writer.setProgressUpdater(new ProgressReporter());
            }

            if (loadOptions.createSchema) {
                db.createMetadataTable(loadOptions);
            }

            if (file.endsWith(".zip") && (file.startsWith("http://") || file.startsWith("https://"))) {
                loadZipFromURI(new URI(file), writer, featureTypeSelectionOptions);
            } else if (file.endsWith(".zip")) {
                loadZip(new File(file), writer, featureTypeSelectionOptions);
            } else if (file.matches(".*\\.[xg]ml")) {
                loadXml(new File(file), writer);
            } else {
                System.err.println(getMessageFormattedString("load.invalid_extension", file));
                return ExitCode.USAGE;
            }

            db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
            // Set feature types list from options, not MutatieInhoud (if input has it)...
            // FIXME if downloaded initial extract has less object types, update will fail -- should set to only encountered
            // feature types
            db.setFeatureTypesEnumMetadata(featureTypeSelectionOptions.getFeatureTypesList());
            db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
            db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");
            db.setMetadataValue(Metadata.TABLE_PREFIX, loadOptions.tablePrefix);
            BGTObjectTableWriter.Progress progress = writer.getProgress();
            if (progress.getMutatieInhoud() != null) {
                db.setMetadataForMutaties(progress.getMutatieInhoud());
                db.setMetadataValue(Metadata.GEOM_FILTER, progress.getMutatieInhoud().getGebied());

                log.info(getMessageFormattedString("load.mutatie",
                        progress.getMutatieInhoud().getMutatieType(),
                        progress.getMutatieInhoud().getLeveringsId()
                ));
            }
            db.getConnection().commit();
        }

        return ExitCode.OK;
    }

    private static boolean isBGTZipEntrySelected(ZipEntry entry, FeatureTypeSelectionOptions featureTypeSelectionOptions, boolean logSkipAsInfo) {
        Set<DeltaCustomDownloadRequest.FeaturetypesEnum> featureTypes = featureTypeSelectionOptions.getFeatureTypesList();
        Pattern p = Pattern.compile("bgt_(.+).[xg]ml");

        Matcher m = p.matcher(entry.getName());
        if (!m.matches()) {
            log.warn(getMessageFormattedString("load.skip_entry", entry.getName()));
            return false;
        }
        String tableName = m.group(1);
        try {
            DeltaCustomDownloadRequest.FeaturetypesEnum featureType = DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(tableName);
            if (!featureTypes.contains(featureType)) {
                String msg = getMessageFormattedString("load.skip_unselected", tableName);
                if (logSkipAsInfo) {
                    log.info(msg);
                } else {
                    log.debug(msg);
                }
                return false;
            } else {
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.warn(getMessageFormattedString("load.skip_unknown_feature_type", entry.getName()));
            return false;
        }
    }

    private void loadZipFromURI(URI downloadURI, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws Exception {
        log.info(getMessageFormattedString("download.downloading_from", downloadURI));
        ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();

        try (InputStream input = new ResumableBGTDownloadInputStream(downloadURI, writer)) {
            CountingInputStream countingInputStream = new CountingInputStream(input);
            progressReporter.setTotalBytesReadFunction(countingInputStream::getByteCount);

            try (ZipInputStream zis = new ZipInputStream(countingInputStream)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    if (isBGTZipEntrySelected(entry, featureTypeSelectionOptions, true)) {
                        progressReporter.startNewFile(entry.getName(), null);
                        writer.write(CloseShieldInputStream.wrap(zis));
                    }
                    entry = zis.getNextEntry();
                }
                progressReporter.reportTotalSummary();
            }
        }
    }

    private void loadZip(File file, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws Exception {
        try(ZipFile zipFile = new ZipFile(file)) {
            List<ZipEntry> entries = zipFile.stream()
                    .filter(entry -> isBGTZipEntrySelected(entry, featureTypeSelectionOptions, false))
                    .collect(Collectors.toList());

            ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();
            if (entries.size() > 1) {
                // Only report total percentage when more than one entry
                Long totalSize = entries.stream().map(ZipEntry::getSize).reduce(0L, Long::sum);
                progressReporter.setTotalBytes(totalSize);
            }
            Long[] previousEntriesBytesRead = new Long[]{0L};
            progressReporter.setTotalBytesReadFunction(() -> previousEntriesBytesRead[0] + writer.getProgress().getBytesRead());

            for (ZipEntry entry: entries) {
                try (InputStream in = zipFile.getInputStream(entry)) {
                    // getSize() will not return -1 because ZipFile uses random access to read the ZIP central directory
                    loadInputStream(entry.getName(), in, entry.getSize(), writer);
                    previousEntriesBytesRead[0] += entry.getSize();
                }
            }
            progressReporter.reportTotalSummary();
        }
    }

    private void loadXml(File file, BGTObjectTableWriter writer) throws Exception {
        try(FileInputStream in = new FileInputStream(file)) {
            loadInputStream(file.getName(), in, file.length(), writer);
        }
    }

    private void loadInputStream(String name, InputStream input, long size, BGTObjectTableWriter writer) throws Exception {
        ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();
        progressReporter.startNewFile(name, size);
        writer.write(input);
    }

    @Override
    public String[] getVersion() {
        return new String[] {
                Utils.getLoaderVersion(),
                Utils.getUserAgent()
        };
    }
}
