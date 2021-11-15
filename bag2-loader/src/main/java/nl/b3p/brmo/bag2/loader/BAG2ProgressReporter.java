/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.schema.ObjectTableWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.util.function.Consumer;

import static nl.b3p.brmo.bag2.loader.BAG2LoaderUtils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2ProgressReporter implements Consumer<ObjectTableWriter.Progress> {
    private static final Log log = LogFactory.getLog(BAG2ProgressReporter.class);

    private final Instant start = Instant.now();

    protected String currentFileName;
    protected Instant currentFileStart;

    protected int splitFileCount;

    protected void log(String msg) {
        log.info(msg);
    }

    protected void status(String msg) {
        log.debug(msg);
    }

    public void startNewFile(String name) {
        this.currentFileName = name;
        this.currentFileStart = Instant.now();
        this.splitFileCount = 0;
    }

    public void startNextSplitFile(String entry) {
        this.splitFileCount++;
    }

    @Override
    public void accept(ObjectTableWriter.Progress progress) {
        BAG2ObjectTableWriter.BAG2Progress bag2Progress = (BAG2ObjectTableWriter.BAG2Progress) progress;

        switch(progress.getStage()) {
            case LOAD_OBJECTS:
                if (progress.getObjectCount() == 0) {
                    log(getMessageFormattedString("progress.loading", currentFileName));
                }
                break;
            case CREATE_PRIMARY_KEY:
                log(getMessageFormattedString("progress.create_primary_key", bag2Progress.getCurrentObjectType().getName()));
                break;
            case CREATE_GEOMETRY_INDEX:
                log(getMessageFormattedString("progress.create_geometry_indexes", bag2Progress.getCurrentObjectType().getName()));
                break;
            case FINISHED:
                log(getMessageFormattedString("progress.finished_file", currentFileName, splitFileCount, bag2Progress.getObjectCount(), formatTimeSince(currentFileStart)));
                //log(String.format("%s: loaded %,d files, %,d total objects in %s", currentFileName, splitFileCount, bag2Progress.getObjectCount(), formatTimeSince(currentFileStart)));
                break;
        }
    }

    public void reportTotalSummary() {
        log(getMessageFormattedString("progress.finished", formatTimeSince(start)));
    }
}
