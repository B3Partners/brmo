/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.bgt.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.MAX_TABLE_LENGTH;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

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
        status(getMessageFormattedString("progress.initializing", name));
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
                    status(getMessageFormattedString("progress.loading", currentFileName));
                }
                break;
            case CREATE_PRIMARY_KEY:
                status(getMessageFormattedString("progress.create_primary_key", currentFileName));
                break;
            case CREATE_GEOMETRY_INDEX:
                status(getMessageFormattedString("progress.create_geometry_indexes", currentFileName));
                break;
            case FINISHED:
                double loadTimeSeconds = Duration.between(currentFileStart, Instant.now()).toMillis() / 1000.0;

                String counts;
                if (progress.getMutatieInhoud() != null && "delta".equals(progress.getMutatieInhoud().getMutatieType())) {
                    counts = String.format("%s, %s: %,6d",
                            progress.getWriter().isCurrentObjectsOnly()
                                    ? String.format("%s: %,6d", getBundleString("progress.removed"), progress.getObjectRemovedCount())  // updated is always 0 when not keeping history
                                    : String.format("%s: %,6d", getBundleString("progress.updated"), progress.getObjectUpdatedCount()), // removed is always 0 when keeping history
                            getBundleString("progress.added"),
                            progress.getObjectCount());
                } else {
                    String historicObjects = "";
                    if (log.isDebugEnabled() && progress.getWriter().isCurrentObjectsOnly()) {
                        historicObjects = String.format(", %,10d %s", progress.getHistoricObjectsCount(), getBundleString("progress.historic_skipped"));
                    }
                    counts = String.format("%,10d %s%s", progress.getObjectCount(), getBundleString("progress.objects"), historicObjects);
                }

                // Align bgt_[max_table_length].gml
                log(String.format("%-" + (MAX_TABLE_LENGTH+8) + "s %10s, %s, %,7.0f %s/s",
                        currentFileName,
                        formatTimeSince(currentFileStart),
                        counts,
                        loadTimeSeconds > 0 ? progress.getObjectCount() / loadTimeSeconds : 0.0,
                        getBundleString("progress.objects")
                ));
                break;
        }
    }

    public void reportTotalSummary() {
        log(getMessageFormattedString("progress.finished", formatTimeSince(start)));
    }
}
