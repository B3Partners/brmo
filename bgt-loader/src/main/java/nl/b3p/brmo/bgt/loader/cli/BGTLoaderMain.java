/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.BGTDatabase;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.bgt.loader.ResumingBGTDownloadInputStream;
import nl.b3p.brmo.bgt.loader.Utils;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.schema.BGTSchemaMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.util.CountingSeekableByteChannel;
import nl.b3p.brmo.util.http.HttpSeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;
import static nl.b3p.brmo.bgt.schema.BGTSchemaMapper.Metadata;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

@Command(name = "bgt-loader", mixinStandardHelpOptions = true, versionProvider = BGTLoaderMain.class,
        resourceBundle = Utils.BUNDLE_NAME, subcommands = {DownloadCommand.class})
public class BGTLoaderMain implements IVersionProvider {
    private static Log log;

    /**
     * init logging.
     *
     * @param standAlone set to {@code false} when using in a preconfigured environment, eg. calling methods from a servlet,
     *                   use {@code true} for commandline usage.
     */
    public static void configureLogging(boolean standAlone) {
        if (standAlone) {
            PropertyConfigurator.configure(BGTLoaderMain.class.getResourceAsStream("/bgt-loader-cli-log4.properties"));
            log = LogFactory.getLog(BGTLoaderMain.class);
            try {
                Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            log = LogFactory.getLog(BGTLoaderMain.class);
        }
    }

    public static void main(String[] args) {
        configureLogging(true);
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
        BGTSchemaMapper bgtSchemaMapper = BGTSchemaMapper.getInstance();
        bgtSchemaMapper.printSchema(dialect, tablePrefix, objectType ->
                tableNames.contains(bgtSchemaMapper.getTableNameForObjectType(objectType, ""))
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

            ProgressReporter progressReporter = cliOptions.isConsoleProgressEnabled()
                    ? new ConsoleProgressReporter()
                    : new ProgressReporter();
            writer.setProgressUpdater(progressReporter);

            if (loadOptions.createSchema) {
                db.createMetadataTable(loadOptions);
            }

            if (file.endsWith(".zip") && (file.startsWith("http://") || file.startsWith("https://"))) {
                loadZipFromURI(new URI(file), writer, featureTypeSelectionOptions, loadOptions, true);
            } else if (file.endsWith(".zip")) {
                loadZip(new File(file), writer, featureTypeSelectionOptions);
            } else if (file.matches(".*\\.[xg]ml")) {
                loadXml(new File(file), writer);
            } else {
                System.err.println(getMessageFormattedString("load.invalid_extension", file));
                return ExitCode.USAGE;
            }

            if (writer.getProgress() == null) {
                System.err.println(getBundleString("error.no_feature_types"));
                return ExitCode.SOFTWARE;
            }
            db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
            // Set feature types list from options, not MutatieInhoud (if input has it)...
            // FIXME if downloaded initial extract has less object types, update will fail -- should set to only encountered
            // feature types
            db.setFeatureTypesEnumMetadata(featureTypeSelectionOptions.getFeatureTypesList());
            db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
            db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");
            db.setMetadataValue(Metadata.TABLE_PREFIX, loadOptions.tablePrefix);
            BGTObjectTableWriter.BGTProgress progress = writer.getProgress();
            if (progress.getMutatieInhoud() != null) {
                db.setMetadataForMutaties(progress.getMutatieInhoud());
                db.setMetadataValue(Metadata.GEOM_FILTER, progress.getMutatieInhoud().getGebied());

                log.info(getMessageFormattedString("load.mutatie",
                        progress.getMutatieInhoud().getMutatieType(),
                        progress.getMutatieInhoud().getLeveringsId()
                ));
            }
            progressReporter.reportTotalSummary();
            db.getConnection().commit();
        }

        return ExitCode.OK;
    }

    private static boolean isBGTZipEntrySelected(String entryName, FeatureTypeSelectionOptions featureTypeSelectionOptions, boolean logSkipAsInfo) {
        Set<DeltaCustomDownloadRequest.FeaturetypesEnum> featureTypes = featureTypeSelectionOptions.getFeatureTypesList();
        Pattern p = Pattern.compile("bgt_(.+).[xg]ml");

        Matcher m = p.matcher(entryName);
        if (!m.matches()) {
            log.warn(getMessageFormattedString("load.skip_entry", entryName));
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
            log.warn(getMessageFormattedString("load.skip_unknown_feature_type", entryName));
            return false;
        }
    }

    public void loadZipFromURI(URI uri, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions, LoadOptions loadOptions, boolean showSelected) throws Exception {
        log.info(getMessageFormattedString("download.downloading_from", uri));
        if (loadOptions.isHttpZipRandomAccess()) {
            loadZipFromURIUsingRandomAccess(uri, writer, featureTypeSelectionOptions, showSelected, loadOptions.isDebugHttpSeeks());
        } else {
            loadZipFromURIUsingStreaming(uri, writer, featureTypeSelectionOptions);
        }
    }

    public void loadZipFromURIUsingRandomAccess(URI uri, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions, boolean showSelected, boolean debugHttpSeeks) throws Exception {
        Instant start = Instant.now();

        // NOTE: it can happen that not all entries from a ZIP are read because of https://issues.apache.org/jira/browse/COMPRESS-584
        // This happened with https://api.pdok.nl/lv/bgt/download/v1_0/cache/2/ebe787b3-e113-4331-ab96-edd1e9bf5aa7/bgt-citygml-nl-nopbp.zip

        try(
                HttpSeekableByteChannel channel = new HttpSeekableByteChannel(uri).withDebug(debugHttpSeeks);
                CountingSeekableByteChannel loggingChannel = new CountingSeekableByteChannel(channel);
                org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(
                        loggingChannel,
                        uri.toString(),
                        "UTF8",
                        false,
                        true
                );
        ) {
            if (debugHttpSeeks) {
                System.out.println();
            }
            int count = 0;
            long uncompressed = 0;
            for (Iterator<ZipArchiveEntry> it = zipFile.getEntries().asIterator(); it.hasNext(); ) {
                ZipArchiveEntry entry = it.next();
                count++;
                uncompressed += entry.getSize();
            }
            log.info(getMessageFormattedString("download.zip.read",
                    formatTimeSince(start),
                    count,
                    byteCountToDisplaySize(channel.size()),
                    byteCountToDisplaySize(uncompressed)));
            if (debugHttpSeeks) {
                log.info(getMessageFormattedString("download.zip.debug-http-seeks.entries",
                        loggingChannel.getNonConsecutiveIops(),
                        channel.getHttpRequestCount(),
                        channel.getBytesRead()));
            }
            loggingChannel.setLoggingEnabled(false);

            ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();
            List<ZipArchiveEntry> selected = new ArrayList<>();
            zipFile.getEntries().asIterator().forEachRemaining(entry -> {
                if (isBGTZipEntrySelected(entry.getName(), featureTypeSelectionOptions, false)) {
                    selected.add(entry);
                }
            });

            if (selected.size() > 1) {
                // Only report total percentage when more than one entry
                Long totalSize = selected.stream().map(ZipArchiveEntry::getSize).reduce(0L, Long::sum);
                Long totalCompressedSize = selected.stream().map(ZipArchiveEntry::getCompressedSize).reduce(0L, Long::sum);
                progressReporter.setTotalBytes(totalSize);
                if (showSelected) {
                    log.info(getMessageFormattedString("download.zip.selected",
                            selected.size(),
                            byteCountToDisplaySize(totalCompressedSize),
                            byteCountToDisplaySize(totalSize)));
                }
            }
            Long[] previousEntriesBytesRead = new Long[]{0L};
            progressReporter.setTotalBytesReadFunction(() -> previousEntriesBytesRead[0] + writer.getProgress().getBytesRead());

            for (ZipArchiveEntry entry: selected) {
                progressReporter.startNewFile(entry.getName(), entry.getSize());
                writer.write(zipFile.getInputStream(entry));
            }
            if (debugHttpSeeks) {
                log.info(getMessageFormattedString("download.zip.debug-http-seeks.totals",
                        loggingChannel.getNonConsecutiveIops(),
                        channel.getHttpRequestCount(),
                        channel.getBytesRead(),
                        byteCountToDisplaySize(channel.getBytesRead())));
            }
        }
    }

    public void loadZipFromURIUsingStreaming(URI downloadURI, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws Exception {
        ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();

        try (InputStream input = new ResumingBGTDownloadInputStream(downloadURI, writer)) {
            CountingInputStream countingInputStream = new CountingInputStream(input);
            progressReporter.setTotalBytesReadFunction(countingInputStream::getByteCount);

            try (ZipInputStream zis = new ZipInputStream(countingInputStream)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    if (isBGTZipEntrySelected(entry.getName(), featureTypeSelectionOptions, true)) {
                        progressReporter.startNewFile(entry.getName(), null);
                        writer.write(CloseShieldInputStream.wrap(zis));
                    }
                    entry = zis.getNextEntry();
                }
            }
        }
    }

    public void loadZip(File file, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws Exception {
        try(ZipFile zipFile = new ZipFile(file)) {
            List<ZipEntry> entries = zipFile.stream()
                    .filter(entry -> isBGTZipEntrySelected(entry.getName(), featureTypeSelectionOptions, false))
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

    public void loadXml(File file, BGTObjectTableWriter writer) throws Exception {
        try(FileInputStream in = new FileInputStream(file)) {
            loadInputStream(file.getName(), in, file.length(), writer);
        }
    }

    public void loadInputStream(String name, InputStream input, long size, BGTObjectTableWriter writer) throws Exception {
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
