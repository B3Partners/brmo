/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.api.CustomDownloadProgress;
import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.COMPLETED;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.PENDING;
import static nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadStatusResponse.StatusEnum.RUNNING;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

class CustomDownloadProgressReporter implements Consumer<CustomDownloadProgress> {
    private static final Log log = LogFactory.getLog(CustomDownloadProgressReporter.class);

    private final boolean printProgress;

    private DeltaCustomDownloadStatusResponse.StatusEnum lastStatus;

    private Instant lastProgress = null;

    public CustomDownloadProgressReporter(boolean printProgress) {
        this.printProgress = printProgress;
    }

    @Override
    public void accept(CustomDownloadProgress progress) {
        if (progress.getStatusApiCalls() == 0 && progress.getDownloadRequestId() != null) {
            log.debug(getMessageFormattedString("download.request_id", progress.getDownloadRequestId()));
            log.info(getBundleString("download.requesting_status"));
        } else {
            DeltaCustomDownloadStatusResponse.StatusEnum status = progress.getStatusResponse().getStatus();
            if (!printProgress) {
                if (lastStatus != status) {
                    log.info(getMessageFormattedString("download.extract_status", status));
                }
                if (status == RUNNING) {
                    // Log progress in logfile every 5 minutes
                    if (lastProgress == null) {
                        lastProgress = Instant.now();
                    } else if (Duration.between(lastProgress, Instant.now()).getSeconds() > 300) {
                        log.info(getMessageFormattedString("download.extract_status", String.format("%s (%2d%%)", status, progress.getStatusResponse().getProgress())));
                        lastProgress = Instant.now();
                    }
                }
            } else {
                if (status == PENDING) {
                    System.out.print("\r" + getMessageFormattedString("download.extract_pending", progress.getTimeSinceStart()));
                } else if (status == RUNNING) {
                    System.out.print("\r" + getMessageFormattedString("download.extract_running", String.format("%3d", progress.getStatusResponse().getProgress()), progress.getTimeSinceStart()));
                } else if (status == COMPLETED) {
                    log.debug(getMessageFormattedString("download.extract_ready", progress.getTimeSinceStart()));
                    System.out.printf("\r%s\r", " ".repeat(30));
                }
            }
            lastStatus = status;
        }
    }
}
