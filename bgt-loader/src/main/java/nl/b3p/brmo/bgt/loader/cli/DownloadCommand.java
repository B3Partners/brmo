package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.api.DeltaApi;
import nl.b3p.brmo.bgt.download.api.DownloadApiUtils;
import nl.b3p.brmo.bgt.download.client.ApiClient;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.Delta;
import nl.b3p.brmo.bgt.download.model.GetDeltasResponse;
import nl.b3p.brmo.bgt.loader.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.loader.IMGeoDb;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.Metadata;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getHEADResponse;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;

@Command(name = "download")
public class DownloadCommand {
    private static final Log log = LogFactory.getLog(DownloadCommand.class);

    @Command(name="initial", sortOptions = false)
    public int initial(
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin ExtractSelectionOptions extractSelectionOptions,
            @Option(names="--no-geo-filter") boolean noGeoFilter,
            @Mixin CLIOptions cliOptions,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        // TODO set alternate URL and timeout from options
        ApiClient client = new ApiClient();

        if (extractSelectionOptions.getGeoFilterWkt() == null && !noGeoFilter) {
            System.err.println("To load an initial extract without a geo filter, specify the --no-geo-filter option");
            return ExitCode.USAGE;
        }

        IMGeoDb db = new IMGeoDb(dbOptions);

        if (loadOptions.createSchema) {
            db.createMetadataTable(loadOptions);
        } else {
            log.info("Connecting to the database... ");
        }

        db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
        db.setFeatureTypesEnumMetadata(extractSelectionOptions.getFeatureTypesList());
        db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
        db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");

        // Close connection while waiting for extract
        db.closeConnection();

        return printApiException(() -> {
            Instant start = Instant.now();
            log.info("Creating custom download... ");

            URI downloadURI = DownloadApiUtils.getCustomDownloadURL(client, null, extractSelectionOptions, new CustomDownloadProgressReporter(cliOptions.isConsoleProgressEnabled()));

            loadFromURI(db, loadOptions, dbOptions, cliOptions, extractSelectionOptions, downloadURI, start);
            db.setMetadataValue(Metadata.DELTA_TIME_TO, null);
            db.getConnection().commit();
            return ExitCode.OK;
        });
    }

    @Command(name="update", sortOptions = false)
    public int update(
            @Mixin DatabaseOptions dbOptions,
            @Mixin CLIOptions cliOptions,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        ApiClient client = new ApiClient();

        log.info("Connecting to the database... ");
        IMGeoDb db = new IMGeoDb(dbOptions);
        String deltaId = db.getMetadata(Metadata.DELTA_ID);
        OffsetDateTime deltaIdTimeTo = null;
        String s = db.getMetadata(Metadata.DELTA_TIME_TO);
        if (s != null && s.length() > 0) {
            deltaIdTimeTo = OffsetDateTime.parse(s);
        }
        if (deltaId == null) {
            System.err.println("Error: no deltaId in metadata table, cannot update");
            return ExitCode.SOFTWARE;
        }
        ExtractSelectionOptions extractSelectionOptions = new ExtractSelectionOptions();
        extractSelectionOptions.setGeoFilterWkt(db.getMetadata(Metadata.GEOM_FILTER));
        if (extractSelectionOptions.getGeoFilterWkt() != null && extractSelectionOptions.getGeoFilterWkt().length() == 0) {
            extractSelectionOptions.setGeoFilterWkt(null);
        }
        extractSelectionOptions.setFeatureTypes(Arrays.asList(db.getMetadata(Metadata.FEATURE_TYPES).split(",")));
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.includeHistory = Boolean.parseBoolean(db.getMetadata(Metadata.INCLUDE_HISTORY));
        loadOptions.linearizeCurves = Boolean.parseBoolean(db.getMetadata(Metadata.LINEARIZE_CURVES));
        loadOptions.tablePrefix = db.getMetadata(Metadata.TABLE_PREFIX);

        log.info(String.format("Currently at delta ID %s%s", deltaId, deltaIdTimeTo != null
                ? ", time to " + DateTimeFormatter.ISO_INSTANT.format(deltaIdTimeTo)
                : ", unknown time to"));
        // Close connection while waiting for extract
        db.closeConnection();

        return printApiException(() -> {
            Instant start = Instant.now();
            log.info("Finding available deltas... ");
            // Note that the afterDeltaId parameter is useless, because the response does not distinguish between
            // "'afterDeltaId' is the latest" and "'afterDeltaId' not found or older than 31 days"
            GetDeltasResponse response = new DeltaApi(client).getDeltas(null, 1, 100);

            // Verify no links to other page, as we expect at most 31 delta's
            if (response.getLinks() != null && !response.getLinks().isEmpty()) {
                throw new IllegalStateException("Did not expect links in GetDeltas response");
            }

            int i;
            for (i = 0; i < response.getDeltas().size(); i++) {
                Delta d = response.getDeltas().get(i);
                if (deltaId.equals(d.getId())) {
                    break;
                }
            }
            if (i == response.getDeltas().size()) {
                // TODO automatically do initial load depending on option
                System.err.println("Error: current delta id not found, new initial load required!");
                return ExitCode.SOFTWARE;
            }

            List<Delta> deltas = response.getDeltas().subList(i+1, response.getDeltas().size());
            if (deltas.isEmpty()) {
                log.info("No new deltas returned, no updates required");
                return ExitCode.OK;
            }

            Delta latestDelta = deltas.get(deltas.size()-1);
            log.info(String.format("Number of deltas to load: %d, latest %s, time to %s", deltas.size(), latestDelta.getId(), latestDelta.getTimeWindow().getTo()));

            for(Delta delta: deltas) {
                log.info(String.format("Creating delta download for delta id %s... ", delta.getId()));
                URI downloadURI = DownloadApiUtils.getCustomDownloadURL(client, delta, extractSelectionOptions, new CustomDownloadProgressReporter(cliOptions.isConsoleProgressEnabled()));
                // TODO: BGTObjectTableWriter does setAutocommit(false) and commit() after each stream for a featuretype
                // is written, maybe use one transaction for all featuretypes?
                loadFromURI(db, loadOptions, dbOptions, cliOptions, extractSelectionOptions, downloadURI, start);
                db.setMetadataValue(Metadata.DELTA_TIME_TO, delta.getTimeWindow().getTo().toString());
                db.getConnection().commit();                
            }

            db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
            db.getConnection().commit();
            return ExitCode.OK;
        });
    }

    private static int printApiException(Callable<Integer> callable) throws Exception {
        try {
            return callable.call();
        } catch(ApiException apiException) {
            System.err.printf("API status code: %d, body: %s\n", apiException.getCode(), apiException.getResponseBody());
            throw apiException;
        }
    }

    private static void loadFromURI(IMGeoDb db, LoadOptions loadOptions, DatabaseOptions dbOptions, CLIOptions cliOptions, ExtractSelectionOptions extractSelectionOptions, URI downloadURI, Instant start) throws Exception {
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        ProgressReporter progressReporter;
        if (cliOptions.isConsoleProgressEnabled()) {
            progressReporter = new ConsoleProgressReporter();
        } else {
            progressReporter = new ProgressReporter();
        }
        writer.setProgressUpdater(progressReporter);

        log.info("Downloading extract from URL: " + downloadURI);

        HttpHeaders headResponseHeaders = getHEADResponse(downloadURI).headers();
        OptionalLong contentLength = headResponseHeaders.firstValueAsLong("Content-Length");
        if (contentLength.isPresent()) {
            log.info("Total size: " + FileUtils.byteCountToDisplaySize(contentLength.getAsLong()));
            progressReporter.setTotalBytes(contentLength.getAsLong());
        }
        // Needed for If-Range header
        //Optional<String> etag = response.headers().firstValue("Etag");

        // Are resume-able downloads with Range requests needed?
        try (InputStream input = new URL(downloadURI.toString()).openConnection().getInputStream()) {
            Instant loadStart = Instant.now();
            CountingInputStream countingInputStream = new CountingInputStream(input);
            progressReporter.setTotalBytesReadFunction(countingInputStream::getByteCount);
            try(ZipInputStream zis = new ZipInputStream(countingInputStream)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    progressReporter.startNewFile(entry.getName(), null);
                    writer.write(CloseShieldInputStream.wrap(zis));
                    entry = zis.getNextEntry();
                }
            }

            db.setMetadataForMutaties(writer.getProgress().getMutatieInhoud());
            // Do not set geom filter from MutatieInhoud, a custom download without geo filter will have gebied
            // "POLYGON ((-100000 200000, 412000 200000, 412000 712000, -100000 712000, -100000 200000))"
            db.setMetadataValue(Metadata.GEOM_FILTER, extractSelectionOptions.getGeoFilterWkt());

            log.info(String.format("Loaded %s extract with delta ID %s in %s%s",
                    writer.getProgress().getMutatieInhoud().getMutatieType(),
                    writer.getProgress().getMutatieInhoud().getLeveringsId(),
                    formatTimeSince(loadStart),
                    start == null ? "" : " (total time " + formatTimeSince(start) + ")"
            ));
        }
    }
}
