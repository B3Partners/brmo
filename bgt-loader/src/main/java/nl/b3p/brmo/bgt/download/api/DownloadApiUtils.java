/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.bgt.download.api;

import nl.b3p.brmo.bgt.download.client.ApiClient;
import nl.b3p.brmo.bgt.download.client.ApiException;
import nl.b3p.brmo.bgt.download.model.Delta;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadResponse;
import nl.b3p.brmo.bgt.loader.cli.ExtractSelectionOptions;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.function.Consumer;

import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.COMPLETED;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class DownloadApiUtils {
    public static URI getCustomDownloadURL(ApiClient client, Delta delta, ExtractSelectionOptions extractSelectionOptions, Consumer<CustomDownloadProgress> progressConsumer) throws ApiException, IOException, InterruptedException {
        DeltaCustomApi deltaCustomApi = new DeltaCustomApi(client);
        DeltaCustomDownloadRequest deltaCustomDownloadRequest = new DeltaCustomDownloadRequest();
        deltaCustomDownloadRequest.setDeltaId(delta == null ? null : delta.getId());
        deltaCustomDownloadRequest.featuretypes(extractSelectionOptions.getFeatureTypesList());
        deltaCustomDownloadRequest.setFormat(DeltaCustomDownloadRequest.FormatEnum.CITYGML);
        deltaCustomDownloadRequest.setGeofilter(extractSelectionOptions.getGeoFilterWkt());

        progressConsumer = progressConsumer == null ? p -> {} : progressConsumer;

        DeltaCustomDownloadResponse downloadResponse = deltaCustomApi.deltaCustomDownload(deltaCustomDownloadRequest);
        String downloadRequestId = downloadResponse.getDownloadRequestId();
        CustomDownloadProgress progress = new CustomDownloadProgress();
        progress.downloadRequestId = downloadRequestId;
        progress.start = Instant.now();
        progressConsumer.accept(progress);

        // TODO make argument
        long waitTime = 1000;
        do {
            progress.statusResponse = deltaCustomApi.deltaCustomDownloadStatus(downloadRequestId);
            progress.statusApiCalls++;
            progress.timeSinceStart = formatTimeSince(progress.start);
            progressConsumer.accept(progress);

            if (progress.statusResponse.getStatus() == COMPLETED) {
                break;
            }

            if (progress.statusApiCalls > 100) {
                waitTime += 1000;
                waitTime = Math.min(30000, waitTime);
            } else if (progress.statusApiCalls > 10) {
                waitTime += 1000;
                waitTime = Math.min(5000, waitTime);
            }
            Thread.sleep(waitTime);

            // TODO timeout
        } while (true);

        if (progress.statusResponse.getStatus() != COMPLETED) {
            throw new IllegalStateException(String.format("Download status for request id \"%s\" is not COMPLETED but \"%s\"", downloadRequestId, progress.statusResponse.getStatus()));
        }

        String downloadUrl = progress.statusResponse.getLinks().getDownload().getHref();
        URI baseUri = URI.create(client.getBaseUri());
        URI fullDownloadUri = baseUri.resolve(downloadUrl);
        return fullDownloadUri;
    }
}
