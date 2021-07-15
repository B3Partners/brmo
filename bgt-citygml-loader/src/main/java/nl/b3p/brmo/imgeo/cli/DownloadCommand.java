package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.bgt.download.api.DeltaApi;
import nl.b3p.brmo.bgt.download.client.ApiClient;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.Delta;
import nl.b3p.brmo.bgt.download.model.GetDeltasResponse;
import nl.b3p.brmo.imgeo.IMGeoObjectTableWriter;
import nl.b3p.brmo.imgeo.api.CustomDownloadProgress;
import nl.b3p.brmo.imgeo.api.DownloadApiUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CountingInputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ExitCode;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.COMPLETED;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.PENDING;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.RUNNING;
import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.Metadata;
import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.getLoaderVersion;
import static nl.b3p.brmo.imgeo.cli.Utils.formatTimeSince;
import static nl.b3p.brmo.imgeo.cli.Utils.getHEADResponse;

@Command(name = "download", mixinStandardHelpOptions = true)
public class DownloadCommand {
    @Command(name="initial", sortOptions = false)
    public int initial(
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin ExtractSelectionOptions extractSelectionOptions,
            @Option(names="--no-geo-filter") boolean noGeoFilter,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        // TODO set alternate URL and timeout from options
        ApiClient client = new ApiClient();

        if (extractSelectionOptions.getGeoFilterWkt() == null && !noGeoFilter) {
            System.err.println("To load an initial extract without a geo filter, specify the --no-geo-filter option");
            return ExitCode.USAGE;
        }

        System.out.print("Connecting to the database... ");
        IMGeoDb db = new IMGeoDb(dbOptions);
        db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
        db.setFeatureTypesEnumMetadata(extractSelectionOptions.getFeatureTypesList());
        db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
        db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");
        System.out.println("ok");
        // Close connection while waiting for extract
        db.closeConnection();

        return printApiException(() -> {
            Instant start = Instant.now();
            System.out.print("Creating custom download... ");

            URI downloadURI = DownloadApiUtils.getCustomDownloadURL(client, null, extractSelectionOptions, cliCustomDownloadProgressConsumer);

            loadFromURI(db, loadOptions, extractSelectionOptions, downloadURI, start);
            db.setMetadataValue(Metadata.DELTA_TIME_TO, null);
            return ExitCode.OK;
        });
    }

    @Command(name="update", sortOptions = false)
    public int update(
            @Mixin DatabaseOptions dbOptions,
            @CommandLine.Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        ApiClient client = new ApiClient();

        System.out.print("Connecting to the database... ");
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

        System.out.printf("ok, at delta ID %s%s\n", deltaId, deltaIdTimeTo != null
                ? ", time to " + DateTimeFormatter.ISO_INSTANT.format(deltaIdTimeTo)
                : ", unknown time to");
        // Close connection while waiting for extract
        db.closeConnection();

        return printApiException(() -> {
            Instant start = Instant.now();
            System.out.println("Finding available deltas... ");
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
                System.out.println("Error: current delta id not found, new initial load required!");
                return ExitCode.SOFTWARE;
            }

            List<Delta> deltas = response.getDeltas().subList(i+1, response.getDeltas().size());
            if (deltas.isEmpty()) {
                System.out.println("No new deltas returned, no updates required");
                return ExitCode.OK;
            }

            Delta latestDelta = deltas.get(deltas.size()-1);
            System.out.printf("Number of deltas to load: %d, latest %s, time to %s\n", deltas.size(), latestDelta.getId(), latestDelta.getTimeWindow().getTo());

            for(Delta delta: deltas) {
                System.out.printf("Creating delta download for delta id %s... ", delta.getId());
                URI downloadURI = DownloadApiUtils.getCustomDownloadURL(client, delta, extractSelectionOptions, cliCustomDownloadProgressConsumer);
                loadFromURI(db, loadOptions, extractSelectionOptions, downloadURI, start);
            }

            db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
            Delta lastDelta = deltas.get(deltas.size()-1);
            db.setMetadataValue(Metadata.DELTA_TIME_TO, lastDelta.getTimeWindow().getTo().toString());
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

    private static final Consumer<CustomDownloadProgress> cliCustomDownloadProgressConsumer = progress -> {
        if (progress.getStatusApiCalls() == 0 && progress.getDownloadRequestId() != null) {
            // Verbose...
            // System.out.println("download request id " + progress.getDownloadRequestId());
            System.out.print("Requesting extract status... ");
        } else {
            if (progress.getStatusResponse().getStatus() == PENDING) {
                System.out.printf("\rExtract is pending for %s...", progress.getTimeSinceStart());
            } else if (progress.getStatusResponse().getStatus() == RUNNING) {
                System.out.printf("\rExtract is running, progress: %d%%, time %s", progress.getStatusResponse().getProgress(), progress.getTimeSinceStart());
            } else if (progress.getStatusResponse().getStatus() == COMPLETED) {
                // Verbose...
                // System.out.printf("\rExtract ready, completed in %s\n", progress.getTimeSinceStart());
                System.out.printf("\r%s\r", " ".repeat(30));
            }
        }
    };

    private static void loadFromURI(IMGeoDb db, LoadOptions loadOptions, ExtractSelectionOptions extractSelectionOptions, URI downloadURI, Instant start) throws Exception {
        System.out.printf("Downloading extract from URL: %s", downloadURI);

        HttpHeaders headResponseHeaders = getHEADResponse(downloadURI).headers();
        OptionalLong contentLength = headResponseHeaders.firstValueAsLong("Content-Length");
        if (contentLength.isPresent()) {
            System.out.printf(" (%s)\n", FileUtils.byteCountToDisplaySize(contentLength.getAsLong()));
        } else {
            System.out.println();
        }
        // Needed for If-Range header
        //Optional<String> etag = response.headers().firstValue("Etag");

        IMGeoObjectTableWriter writer = db.createObjectTableWriter(loadOptions);

        // Are resume-able downloads with Range requests needed?
        try (InputStream input = new URL(downloadURI.toString()).openConnection().getInputStream()) {
            Instant loadStart = Instant.now();
            CountingInputStream countingInputStream = new CountingInputStream(input);
            ZipInputStream zis = new ZipInputStream(countingInputStream);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                System.out.print("Loading zip entry " + entry.getName());
                Instant zipEntryStart = Instant.now();
                ZipEntry finalEntry = entry;
                writer.setProgressUpdater(() -> System.out.printf("\rTotal extract %.1f%% loaded - file \"%s\" time %s, %,d objects",
                        100.0 / contentLength.orElse(0) * countingInputStream.getByteCount(),
                        finalEntry.getName(),
                        formatTimeSince(zipEntryStart),
                        writer.getObjectCount() + writer.getObjectUpdatedCount()
                ));
                writer.write(CloseShieldInputStream.wrap(zis));
                String count;
                if (writer.getMutatieInhoud() != null && "delta".equals(writer.getMutatieInhoud().getMutatieType())) {
                    count = String.format("%s, added: %,d",
                            writer.isCurrentObjectsOnly()
                                    ? String.format("removed: %,d", writer.getObjectRemovedCount())  // updated is always 0 when not keeping history
                                    : String.format("updated: %,d", writer.getObjectUpdatedCount()), // removed is always 0 when keeping history
                            writer.getObjectCount());
                } else {
                    count = String.format("%,d objects", writer.getObjectCount());
                }
                double loadTimeSeconds = Duration.between(zipEntryStart, Instant.now()).toMillis() / 1000.0;
                System.out.printf("\r%s (%s): time %s, %s, %,.0f objects/s%s\n",
                        entry.getName(),
                        FileUtils.byteCountToDisplaySize(countingInputStream.getByteCount()),
                        formatTimeSince(zipEntryStart),
                        count,
                        writer.getObjectCount() / loadTimeSeconds,
                        " ".repeat(50)
                );
                entry = zis.getNextEntry();
            }

            db.setMetadataForMutaties(writer.getMutatieInhoud());
            // Do not set geom filter from MutatieInhoud, a custom download without geo filter will have gebied
            // "POLYGON ((-100000 200000, 412000 200000, 412000 712000, -100000 712000, -100000 200000))"
            db.setMetadataValue(Metadata.GEOM_FILTER, extractSelectionOptions.getGeoFilterWkt());

            System.out.printf("\rLoaded %s extract with delta ID %s in %s%s\n",
                    writer.getMutatieInhoud().getMutatieType(),
                    writer.getMutatieInhoud().getLeveringsId(),
                    formatTimeSince(loadStart),
                    start == null ? "" : " (total time " + formatTimeSince(start) + ")"
            );
        }
    }
}
