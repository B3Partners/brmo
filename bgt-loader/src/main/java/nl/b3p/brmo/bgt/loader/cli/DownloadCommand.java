/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.api.CustomDownloadProgress;
import nl.b3p.brmo.bgt.download.api.DeltaApi;
import nl.b3p.brmo.bgt.download.api.DownloadApiUtils;
import nl.b3p.brmo.bgt.download.client.ApiClient;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.Delta;
import nl.b3p.brmo.bgt.download.model.GetDeltasResponse;
import nl.b3p.brmo.bgt.loader.BGTDatabase;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getBrmoVersion;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;
import static nl.b3p.brmo.bgt.schema.BGTSchemaMapper.Metadata;

@Command(name = "download", mixinStandardHelpOptions = true)
public class DownloadCommand {
    private static final Log log = LogFactory.getLog(DownloadCommand.class);

    private static final String PREDEFINED_FULL_DELTA_URI = "https://api.pdok.nl/lv/bgt/download/v1_0/delta/predefined/bgt-citygml-nl-delta.zip";

    /** zodat we een JNDI database kunne gebruiken. */
    private BGTDatabase bgtDatabase = null;

    public static ApiClient getApiClient(URI baseUri) {
        ApiClient client = new ApiClient();
        if (baseUri != null) {
            client.updateBaseUri(baseUri.toString());
        }
        client.setRequestInterceptor(builder -> builder.headers("User-Agent", getUserAgent()));
        return client;
    }

    private BGTObjectTableWriter createWriter(BGTDatabase db, DatabaseOptions dbOptions, LoadOptions loadOptions, CLIOptions cliOptions) throws SQLException {
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        ProgressReporter progressReporter;
        if (cliOptions.isConsoleProgressEnabled()) {
            progressReporter = new ConsoleProgressReporter();
        } else {
            progressReporter = new ProgressReporter();
        }
        writer.setProgressUpdater(progressReporter);
        return writer;
    }

    @Command(name="initial", sortOptions = false)
    public int initial(
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin ExtractSelectionOptions extractSelectionOptions,
            @Option(names="--no-geo-filter") boolean noGeoFilter,
            @Option(names="--download-service", hidden = true) URI downloadServiceURI,
            @Mixin CLIOptions cliOptions,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        if (extractSelectionOptions.getGeoFilterWkt() == null && !noGeoFilter) {
            System.err.println(getBundleString("download.no_geo_filter"));
            return ExitCode.USAGE;
        }

        log.info(getUserAgent());

        try(BGTDatabase db = this.getBGTdatabase(dbOptions)) {
            if (loadOptions.createSchema) {
                db.createMetadataTable(loadOptions);
            } else {
                log.info(getBundleString("download.connect_db"));
            }

            db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
            db.setMetadataValue(Metadata.BRMOVERSIE, getBrmoVersion());
            db.setFeatureTypesEnumMetadata(extractSelectionOptions.getFeatureTypesList());
            db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
            db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");
            db.setMetadataValue(Metadata.TABLE_PREFIX, loadOptions.tablePrefix);

            Instant start = null;
            URI uri;
            if (noGeoFilter) {
                uri = new URI(PREDEFINED_FULL_DELTA_URI);
            } else {
                // Close connection while waiting for extract
                db.close();
                start = Instant.now(); // Record total time waiting for extract
                uri = getCustomDownloadURI(downloadServiceURI, extractSelectionOptions, new CustomDownloadProgressReporter(cliOptions.isConsoleProgressEnabled()));
            }
            BGTObjectTableWriter writer = createWriter(db, dbOptions, loadOptions, cliOptions);
            loadZipFromURI(uri, db, writer, extractSelectionOptions, loadOptions, noGeoFilter, start);

            db.setMetadataValue(Metadata.DELTA_TIME_TO, null);
            // Do not set geom filter from MutatieInhoud, a custom download without geo filter will have gebied
            // "POLYGON ((-100000 200000, 412000 200000, 412000 712000, -100000 712000, -100000 200000))"
            db.setMetadataValue(Metadata.GEOM_FILTER, extractSelectionOptions.getGeoFilterWkt());

            db.getConnection().commit();
            return ExitCode.OK;
        }
    }

    /**
     * set a preconfigured database instead of using one created in the command using the dbOtions. Useful when using a JDNI database.
     *
     * @param bgtDatabase the BGT database to use for any issued commands
     */
    public void setBGTDatabase(BGTDatabase bgtDatabase) {
        this.bgtDatabase = bgtDatabase;
    }

    private BGTDatabase getBGTdatabase(DatabaseOptions dbOptions) throws ClassNotFoundException {
        if (null == this.bgtDatabase) {
            return new BGTDatabase(dbOptions);
        } else return this.bgtDatabase;
    }

    private static URI getCustomDownloadURI(URI downloadServiceURI, ExtractSelectionOptions extractSelectionOptions, Consumer<CustomDownloadProgress> progressConsumer) throws ApiException, InterruptedException {
        try {
            log.info(getBundleString("download.create"));
            ApiClient client = getApiClient(downloadServiceURI);
            return DownloadApiUtils.getCustomDownloadURL(client, null, extractSelectionOptions, progressConsumer);
        } catch(ApiException apiException) {
            printApiException(apiException);
            throw apiException;
        }
    }

    @Command(name="update", sortOptions = false)
    public int update(
            @Mixin DatabaseOptions dbOptions,
            @Mixin CLIOptions cliOptions,
            @Option(names="--download-service", hidden = true) URI downloadServiceURI,
            @Option(names="--no-http-zip-random-access", negatable = true, hidden = true) boolean noHttpZipRandomAccess,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {
        log.info(getUserAgent());

        ApiClient client = getApiClient(downloadServiceURI);

        log.info(getBundleString("download.connect_db"));
        try(BGTDatabase db = getBGTdatabase(dbOptions)) {
            String deltaId = db.getMetadata(Metadata.DELTA_ID);
            OffsetDateTime deltaIdTimeTo = null;
            String s = db.getMetadata(Metadata.DELTA_TIME_TO);
            if (s != null && s.length() > 0) {
                deltaIdTimeTo = OffsetDateTime.parse(s);
            }
            if (deltaId == null) {
                System.err.println(getBundleString("download.no_delta_id"));
                return ExitCode.SOFTWARE;
            }
            ExtractSelectionOptions extractSelectionOptions = new ExtractSelectionOptions();
            extractSelectionOptions.setGeoFilterWkt(db.getMetadata(Metadata.GEOM_FILTER));
            if (extractSelectionOptions.getGeoFilterWkt() != null && extractSelectionOptions.getGeoFilterWkt().length() == 0) {
                extractSelectionOptions.setGeoFilterWkt(null);
            }
            extractSelectionOptions.setFeatureTypes(Arrays.asList(db.getMetadata(Metadata.FEATURE_TYPES).split(",")));
            LoadOptions loadOptions = new LoadOptions();
            loadOptions.setHttpZipRandomAccess(!noHttpZipRandomAccess);
            loadOptions.includeHistory = Boolean.parseBoolean(db.getMetadata(Metadata.INCLUDE_HISTORY));
            loadOptions.linearizeCurves = Boolean.parseBoolean(db.getMetadata(Metadata.LINEARIZE_CURVES));
            loadOptions.tablePrefix = db.getMetadata(Metadata.TABLE_PREFIX);

            log.info(getMessageFormattedString("download.current_delta_id", deltaId) + ", " +
                    (deltaIdTimeTo != null
                            ? getMessageFormattedString("download.current_delta_time", DateTimeFormatter.ISO_INSTANT.format(deltaIdTimeTo))
                            : getBundleString("download.current_delta_time_unknown")));

            try {
                Instant start = Instant.now();
                log.info(getBundleString("download.loading_deltas"));
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
                    System.err.println(getBundleString("download.current_delta_not_found"));
                    return ExitCode.SOFTWARE;
                }

                List<Delta> deltas = response.getDeltas().subList(i + 1, response.getDeltas().size());
                if (deltas.isEmpty()) {
                    log.info(getBundleString("download.uptodate"));
                    return ExitCode.OK;
                }

                Delta latestDelta = deltas.get(deltas.size() - 1);
                log.info(getMessageFormattedString("download.updates_available", deltas.size(), latestDelta.getId(), latestDelta.getTimeWindow().getTo()));

                BGTObjectTableWriter writer = createWriter(db, dbOptions, loadOptions, cliOptions);

                int deltaCount = 1;
                for (Delta delta: deltas) {
                    log.info(getMessageFormattedString("download.creating_download", deltaCount++, deltas.size(), delta.getId()));
                    URI uri = DownloadApiUtils.getCustomDownloadURL(client, delta, extractSelectionOptions, new CustomDownloadProgressReporter(cliOptions.isConsoleProgressEnabled()));
                    // TODO: BGTObjectTableWriter does setAutocommit(false) and commit() after each stream for a feature type
                    // is written, maybe use one transaction for all feature types?
                    loadZipFromURI(uri, db, writer, extractSelectionOptions, loadOptions, false, start);
                    db.setMetadataValue(Metadata.DELTA_TIME_TO, delta.getTimeWindow().getTo().toString());
                    db.getConnection().commit();
                }

                db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
                db.getConnection().commit();
                return ExitCode.OK;
            } catch(ApiException apiException) {
                printApiException(apiException);
                throw apiException;
            }
        }
    }

    private static void printApiException(ApiException apiException) {
        log.error(String.format("API status code: %d, body: %s\n", apiException.getCode(), apiException.getResponseBody()));
    }

    private static void loadZipFromURI(URI uri, BGTDatabase db, BGTObjectTableWriter writer, ExtractSelectionOptions extractSelectionOptions, LoadOptions loadOptions, boolean showSelected, Instant start) throws Exception {
        Instant loadStart = Instant.now();
        new BGTLoaderMain().loadZipFromURI(uri, writer, extractSelectionOptions, loadOptions, showSelected);
        db.setMetadataForMutaties(writer.getProgress().getMutatieInhoud());
        log.info(getMessageFormattedString("download.complete",
                getBundleString("download.mutatietype." + writer.getProgress().getMutatieInhoud().getMutatieType()),
                writer.getProgress().getMutatieInhoud().getLeveringsId(),
                formatTimeSince(loadStart)) +
                (start == null ? "" : " " + getMessageFormattedString("download.complete_total", formatTimeSince(start)))
        );
    }
}
