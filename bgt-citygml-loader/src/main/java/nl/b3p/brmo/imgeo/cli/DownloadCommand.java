package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.bgt.download.api.DeltaApi;
import nl.b3p.brmo.bgt.download.api.DeltaCustomApi;
import nl.b3p.brmo.bgt.download.client.ApiClient;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.Delta;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadResponse;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse;
import nl.b3p.brmo.bgt.download.model.GetDeltasResponse;
import nl.b3p.brmo.imgeo.IMGeoObjectTableWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.COMPLETED;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.PENDING;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.RUNNING;
import static nl.b3p.brmo.imgeo.cli.Utils.formatTimeSince;

@Command(name = "download")
public class DownloadCommand {
    @Command(name="init")
    public void init(
            @CommandLine.Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Mixin ExtractSelectionOptions extractSelectionOptions,
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions) throws Exception {

        // TODO set alternate URL and timeout from options
        ApiClient client = new ApiClient();

        printApiException(() -> {
            DeltaCustomApi deltaCustomApi = new DeltaCustomApi(client);
            DeltaCustomDownloadRequest deltaCustomDownloadRequest = new DeltaCustomDownloadRequest();
            // Set to null to get initial load
            deltaCustomDownloadRequest.setDeltaId(null);
            deltaCustomDownloadRequest.featuretypes(extractSelectionOptions.getFeatureTypesList());
            deltaCustomDownloadRequest.setFormat(DeltaCustomDownloadRequest.FormatEnum.CITYGML);
            deltaCustomDownloadRequest.setGeofilter(extractSelectionOptions.geoFilterWkt);
            // Create custom download requests for all feature types together
            // Optimization: split download request per feature type, and load each when they are completed (possibly parallel)
            System.out.print("Creating custom download... ");

            DeltaCustomDownloadResponse downloadResponse = deltaCustomApi.deltaCustomDownload(deltaCustomDownloadRequest);
            String downloadRequestId = downloadResponse.getDownloadRequestId();
            System.out.println("id " + downloadRequestId);

            // Wait for custom download request to complete
            System.out.print("Requesting extract status... ");
            DeltaCustomDownloadStatusResponse downloadStatusResponse;
            long waitTime = 1000;
            int calls = 0;
            Instant start = Instant.now();
            do {
                downloadStatusResponse = deltaCustomApi.deltaCustomDownloadStatus(downloadRequestId);
                calls++;
                if (downloadStatusResponse.getStatus() == COMPLETED) {
                    break;
                }
                String timeSince = formatTimeSince(start);

                if (downloadStatusResponse.getStatus() == PENDING) {
                    // TODO print wait time
                    System.out.printf("\rExtract is pending for %s...               ", timeSince);
                } else if (downloadStatusResponse.getStatus() == RUNNING) {
                    System.out.printf("\rExtract is running, progress: %d%%, time %s              ", downloadStatusResponse.getProgress(), timeSince);
                }
                if (calls > 100) {
                    waitTime += 1000;
                    waitTime = Math.min(30000, waitTime);
                } else if (calls > 10) {
                    waitTime += 1000;
                    waitTime = Math.min(5000, waitTime);
                }
                Thread.sleep(waitTime);

                // TODO timeout
            } while (true);
            System.out.printf("\rExtract ready to download, time: %s                          \n", formatTimeSince(start));

            if (downloadStatusResponse.getStatus() != COMPLETED) {
                throw new IllegalStateException(String.format("Download status for request id \"%s\" is not COMPLETED but \"%s\"", downloadRequestId, downloadStatusResponse.getStatus()));
            }

            // Download the data and load it (streaming or download fully)
            String downloadUrl = downloadStatusResponse.getLinks().getDownload().getHref();

            URI baseUri = URI.create(client.getBaseUri());
            URI fullDownloadUri = baseUri.resolve(downloadUrl);

            System.out.println("Downloading extract from URL: " + fullDownloadUri);

            // Get file size and ETag
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(fullDownloadUri)
                    .method("HEAD", noBody())
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(String.format("HEAD for URI \"%s\" returned status code %d", fullDownloadUri, response.statusCode()));
            }
            OptionalLong contentLength = response.headers().firstValueAsLong("Content-Length");
            if (contentLength.isPresent()) {
                System.out.println("Extract size: " + FileUtils.byteCountToDisplaySize(contentLength.getAsLong()));
            }
            // Needed for If-Range header
            //Optional<String> etag = response.headers().firstValue("Etag");

            IMGeoDb db = new IMGeoDb(dbOptions);
            IMGeoObjectTableWriter writer = db.createObjectTableWriter(loadOptions);

            InputStream input = new URL(fullDownloadUri.toString()).openConnection().getInputStream();

            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry entry = zis.getNextEntry();
            while(entry != null) {
                System.out.println("Processing ZIP entry " + entry.getName());
                long size = entry.getSize();
                writer.write(CloseShieldInputStream.wrap(zis));
                entry = zis.getNextEntry();
            }

            return null;
        });

        // Insert deltaId, loadoptions and contentselection metadata
    }

    @Command(name="update")
    public void update(
            @CommandLine.Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Mixin DatabaseOptions dbOptions) throws Exception {

        ApiClient client = new ApiClient();

        printApiException(() -> {
            System.out.print("Finding latest deltaId... ");
            GetDeltasResponse response = new DeltaApi(client).getDeltas(null, 1, 100);
            Delta latestDelta = null;

            // Verify no links to other page, as we expect at most 31 delta's
            if (response.getLinks() != null && !response.getLinks().isEmpty()) {
                throw new IllegalStateException("Did not expect links in GetDeltas response");
            }

            List<Delta> deltas = response.getDeltas();
            if (deltas != null && !deltas.isEmpty()) {
                latestDelta = deltas.get(deltas.size()-1);
                System.out.println("found " + latestDelta.getId() + ", time to " + latestDelta.getTimeWindow().getTo());
            } else {
                System.out.println("couldn't find latest delta");
            }
            return null;
        });
    }

    private static void printApiException(Callable<Void> callable) throws Exception {
        try {
            callable.call();
        } catch(ApiException apiException) {
            System.err.printf("API status code: %d, body: %s\n", apiException.getCode(), apiException.getResponseBody());
            throw apiException;
        }
    }
}
