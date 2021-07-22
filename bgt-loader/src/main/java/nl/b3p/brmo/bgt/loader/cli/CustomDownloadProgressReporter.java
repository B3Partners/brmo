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

class CustomDownloadProgressReporter implements Consumer<CustomDownloadProgress> {
    private static final Log log = LogFactory.getLog(CustomDownloadProgressReporter.class);

    private final boolean showProgress;

    private DeltaCustomDownloadStatusResponse.StatusEnum lastStatus;

    private Instant lastProgress = null;

    public CustomDownloadProgressReporter(boolean printProgress) {
        this.showProgress = printProgress;
    }

    @Override
    public void accept(CustomDownloadProgress progress) {
        if (progress.getStatusApiCalls() == 0 && progress.getDownloadRequestId() != null) {
            log.debug("Download request ID: " + progress.getDownloadRequestId());
            log.info("Requesting extract status... ");
        } else {
            DeltaCustomDownloadStatusResponse.StatusEnum status = progress.getStatusResponse().getStatus();
            if (!showProgress) {
                if (lastStatus != status) {
                    log.info("Extract status: " + status);
                }
                if (status == RUNNING) {
                    // Log progress in logfile every 5 minutes
                    if (lastProgress == null) {
                        lastProgress = Instant.now();
                    } else if (Duration.between(lastProgress, Instant.now()).getSeconds() > 300) {
                        log.info(String.format("Extract status: %s (%2d%%)", status, progress.getStatusResponse().getProgress()));
                        lastProgress = Instant.now();
                    }
                }
            } else {
                if (status == PENDING) {
                    System.out.printf("\rExtract is pending for %s...", progress.getTimeSinceStart());
                } else if (status == RUNNING) {
                    System.out.printf("\rExtract is running, progress: %3d%%, time %s", progress.getStatusResponse().getProgress(), progress.getTimeSinceStart());
                } else if (status == COMPLETED) {
                    log.debug(String.format("Extract ready, completed in %s\n", progress.getTimeSinceStart()));
                    System.out.printf("\r%s\r", " ".repeat(30));
                }
            }
            lastStatus = status;
        }
    }
}
