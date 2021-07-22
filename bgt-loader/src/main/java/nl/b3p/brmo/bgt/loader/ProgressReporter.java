package nl.b3p.brmo.bgt.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.MAX_TABLE_LENGTH;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class ProgressReporter implements Consumer<BGTObjectTableWriter.Progress> {
    private static final Log log = LogFactory.getLog(ProgressReporter.class);

    private final Instant start = Instant.now();
    private Long totalBytes;
    private Supplier<Long> totalBytesReadFunction;

    private String currentFileName;
    private Instant currentFileStart = Instant.now();
    private Long currentFileSize;

    public Instant getStart() {
        return start;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public Supplier<Long> getTotalBytesReadFunction() {
        return totalBytesReadFunction;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public Instant getCurrentFileStart() {
        return currentFileStart;
    }

    public Long getCurrentFileSize() {
        return currentFileSize;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setTotalBytesReadFunction(Supplier<Long> totalBytesReadFunction) {
        this.totalBytesReadFunction = totalBytesReadFunction;
    }

    public void startNewFile(String name, Long size) {
        this.currentFileName = name;
        this.currentFileSize = size;
        this.currentFileStart = Instant.now();
        status(name + ": initializing...");
    }

    protected void log(String msg) {
        log.info(msg);
    }

    protected void status(String msg) {
        log.debug(msg);
    }

    @Override
    public void accept(BGTObjectTableWriter.Progress progress) {
        switch(progress.getStage()) {
            case LOAD_OBJECTS:
                if (progress.getObjectCount() == 0) {
                    status(currentFileName + ": loading...");
                }
/*                System.out.printf("\r%s (%s): %.1f%% - time %s, %,d objects",
                        currentFileName,
                        FileUtils.byteCountToDisplaySize(currentFileSize),
                        100.0 / currentFileSize * currentFileBytesReadFunction.get(),
                        formatTimeSince(currentFileStart),
                        progress.getObjectCount()
                );*/
                break;
            case CREATE_PRIMARY_KEY:
                status(currentFileName +": creating primary key...");
                break;
            case CREATE_GEOMETRY_INDEX:
                status(currentFileName +": creating geometry indexes...");
                break;
            case FINISHED:
                double loadTimeSeconds = Duration.between(currentFileStart, Instant.now()).toMillis() / 1000.0;

                String counts;
                if (progress.getMutatieInhoud() != null && "delta".equals(progress.getMutatieInhoud().getMutatieType())) {
                    counts = String.format("%s, added: %,6d",
                            progress.getWriter().isCurrentObjectsOnly()
                                    ? String.format("removed: %,6d", progress.getObjectRemovedCount())  // updated is always 0 when not keeping history
                                    : String.format("updated: %,6d", progress.getObjectUpdatedCount()), // removed is always 0 when keeping history
                            progress.getObjectCount());
                } else {
                    String historicObjects = progress.getWriter().isCurrentObjectsOnly() ? String.format(", %,10d historic objects skipped", progress.getHistoricObjectsCount()) : "";
                    counts = String.format("%,10d objects%s", progress.getObjectCount(), historicObjects);
                }

                // Align bgt_[max_table_length].gml
                log(String.format("%-" + (MAX_TABLE_LENGTH+8) + "s %10s, %s, %,7.0f objects/s",
                        currentFileName,
                        formatTimeSince(currentFileStart),
                        counts,
                        loadTimeSeconds > 0 ? progress.getObjectCount() / loadTimeSeconds : 0.0
                ));
                break;
        }
    }

    public void reportTotalSummary() {
        log("Finished writing all tables in " + formatTimeSince(start));
    }

}
