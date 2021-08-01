/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.BGTDatabase;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.schema.BGTSchemaMapper;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.bgt.loader.ResumableBGTDownloadInputStream;
import nl.b3p.brmo.bgt.loader.Utils;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.util.HttpStartRangeInputStreamProvider;
import nl.b3p.brmo.util.ResumableInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.IOUtils;
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

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static nl.b3p.brmo.bgt.schema.BGTSchemaMapper.Metadata;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;

@Command(name = "bgt-loader", mixinStandardHelpOptions = true, versionProvider = BGTLoaderMain.class,
        resourceBundle = Utils.BUNDLE_NAME, subcommands = {DownloadCommand.class})
public class BGTLoaderMain implements IVersionProvider {
    private static Log log;

    public static void configureLogging() {
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

            if (cliOptions.isConsoleProgressEnabled()) {
                writer.setProgressUpdater(new ConsoleProgressReporter());
            } else {
                writer.setProgressUpdater(new ProgressReporter());
            }

            if (loadOptions.createSchema) {
                db.createMetadataTable(loadOptions);
            }

            if (file.endsWith(".zip") && (file.startsWith("http://") || file.startsWith("https://"))) {
                loadZipFromRandomAccessURI(new URI(file), writer, featureTypeSelectionOptions);
            } else if (file.endsWith(".zip")) {
                loadZip(new File(file), writer, featureTypeSelectionOptions);
            } else if (file.matches(".*\\.[xg]ml")) {
                loadXml(new File(file), writer);
            } else {
                System.err.println(getMessageFormattedString("load.invalid_extension", file));
                return ExitCode.USAGE;
            }

            if (writer.getProgress() == null) {
                System.err.println("Error: No feature types loaded");
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

    private void loadZipFromRandomAccessURI(URI downloadURI, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws Exception {
        final boolean[] logIt = {true};
        SeekableByteChannel channel = new SeekableByteChannel() {
            private ResumableInputStream resumableInputStream;
            private long position;
            private Long nextReadPosition;
            private Long contentLength;

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() throws IOException {
                if (resumableInputStream != null) {
                    resumableInputStream.close();
                }
            }

            @Override
            public int read(ByteBuffer byteBuffer) throws IOException {
                if (nextReadPosition != null && nextReadPosition != position) {
                    if (resumableInputStream != null) {
                        if(logIt[0]) System.out.print(" [Close HTTP at pos " + position + "] ");
                        resumableInputStream.close();
                        resumableInputStream = null;
                    }
                    position = nextReadPosition;
                    nextReadPosition = null;
                }

                if (resumableInputStream == null) {
                    if(logIt[0]) System.out.print(" [GET at position " + position + "] ");
                    resumableInputStream = new ResumableInputStream(new HttpStartRangeInputStreamProvider(downloadURI, HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1).build()) {
/*                        @Override
                        public void afterHttpRequest(HttpResponse<InputStream> response) {
                            if (!response.headers().map().containsKey("Content-Range")) {
                                OptionalLong contentLengthHeader = response.headers().firstValueAsLong("Content-Length");
                                if (contentLengthHeader.isPresent()) {
                                    contentLength = contentLengthHeader.getAsLong();
                                    System.out.println("Content length: " + contentLength);
                                }
                            }
                        }*/
                    }, position);
                }
                //System.out.print("read bytebuffer: " + byteBuffer.toString() + " from position " + position +" read ");
                // TODO read into array of byteBuffer if hasArray()
                byte[] b = new byte[Math.min(byteBuffer.remaining(), 16384)];
                int read = resumableInputStream.read(b);
                if (read > 0) {
                    byteBuffer.put(b, 0, read);
                    position += read;
                }
                //System.out.println(read + " bytes");
                return read;
            }

            @Override
            public int write(ByteBuffer byteBuffer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public long position() {
                return nextReadPosition != null ? nextReadPosition : position;
            }

            @Override
            public SeekableByteChannel position(long l) throws IOException {
                nextReadPosition = l;
                return this;
            }

            @Override
            public long size() {
                if (contentLength == null) {
                    if (position > 0) {
                        throw new UnsupportedOperationException("Content-length unknown");
                    }
                    System.out.println("TODO HEAD request");
                    contentLength = 50979311967L;// 19853996594L;
                }
                return contentLength;
            }

            @Override
            public SeekableByteChannel truncate(long l) {
                throw new UnsupportedOperationException();
            }
        };
        Path p = Paths.get("/home/matthijsln/bgt/bgt-citygml-nl-nopbp.zip");
        SeekableByteChannel fileChannel = channel;//FileChannel.open(p);
        channel = new SeekableByteChannel() {
            Long size = null;
            int seeks = 0;
            long bytesRead = 0;
            boolean repositioned = false;

            @Override
            public int read(ByteBuffer byteBuffer) throws IOException {
                if (repositioned) {
                    if(logIt[0]) System.out.printf("position %15d: read into buffer %s", fileChannel.position(), byteBuffer);
                    repositioned = false;
                } else {
                    if(logIt[0]) System.out.printf("continue %15d: read into buffer %s", fileChannel.position(), byteBuffer);
                }
                int read = fileChannel.read(byteBuffer);
                if(logIt[0]) System.out.printf(", read %s, contents: %s\n", read, DatatypeConverter.printHexBinary((byteBuffer.array())));
                bytesRead += read;
                return read;
            }

            @Override
            public int write(ByteBuffer byteBuffer) throws IOException {
                return 0;
            }

            @Override
            public long position() throws IOException {
                return fileChannel.position();
            }

            @Override
            public SeekableByteChannel position(long l) throws IOException {
                if (fileChannel.position() != l) {
                    fileChannel.position(l);
                    seeks++;
                    repositioned = true;
                }
                return this;
            }

            @Override
            public long size() throws IOException {
                if (size == null) {
                    size = fileChannel.size();
                    System.out.println("Size: " + size);
                }
                return size;
            }

            @Override
            public SeekableByteChannel truncate(long l) throws IOException {
                return null;
            }

            @Override
            public boolean isOpen() {
                return fileChannel.isOpen();
            }

            @Override
            public void close() throws IOException {
                fileChannel.close();
                System.out.printf("Closed, total seeks: %d, bytes read: %d\n", seeks, bytesRead);
            }
        };
        org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(
                channel,
                downloadURI.toString(),
                "UTF8",
                false,
                true
        );
        int count = 0;
        for (Iterator<ZipArchiveEntry> it = zipFile.getEntries().asIterator(); it.hasNext(); ) {
            ZipArchiveEntry entry = it.next();
            count++;
            System.out.printf("%31s: %15d size, %15d compressed, %15d offset\n", entry, entry.getSize(), entry.getCompressedSize(), entry.getDataOffset());
        }
        System.out.println("Entries: " + count);
        logIt[0] = false;

        log.info(getMessageFormattedString("download.downloading_from", downloadURI));
        ProgressReporter progressReporter = (ProgressReporter) writer.getProgressUpdater();
        List<ZipArchiveEntry> selected = new ArrayList<>();
        zipFile.getEntries().asIterator().forEachRemaining(entry -> {
            if (isBGTZipEntrySelected(entry.getName(), featureTypeSelectionOptions, true)) {
                selected.add(entry);
            }
        });

        if (selected.size() > 1) {
            // Only report total percentage when more than one entry
            Long totalSize = selected.stream().map(ZipArchiveEntry::getSize).reduce(0L, Long::sum);
            progressReporter.setTotalBytes(totalSize);
            System.out.printf("Selected entries: %d, total uncompressed bytes: %d", selected.size(), totalSize);
        }
        Long[] previousEntriesBytesRead = new Long[]{0L};
        progressReporter.setTotalBytesReadFunction(() -> previousEntriesBytesRead[0] + writer.getProgress().getBytesRead());

        for(ZipArchiveEntry entry: selected) {
            progressReporter.startNewFile(entry.getName(), entry.getSize());
            writer.write(zipFile.getInputStream(entry));
        }

        zipFile.close();
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
                    if (isBGTZipEntrySelected(entry.getName(), featureTypeSelectionOptions, true)) {
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
