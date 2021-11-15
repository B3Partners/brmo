/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.schema.ObjectTableWriter;

import java.time.Instant;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.schema.ObjectTableWriter.Stage.FINISHED;

public class ConsoleProgressReporter extends ProgressReporter {
    Instant totalStart = Instant.now();

    @Override
    protected void log(String msg) {
        System.out.print("\r" + msg);
    }

    @Override
    protected void status(String msg) {
        System.out.print("\r" + msg + " ".repeat(40));
    }

    @Override
    public void reportTotalSummary() {
        super.reportTotalSummary();
        System.out.println();
    }

    @Override
    public void accept(ObjectTableWriter.Progress genericProgress) {
        super.accept(genericProgress);
        BGTObjectTableWriter.BGTProgress progress = (BGTObjectTableWriter.BGTProgress)genericProgress;

        if (progress.getStage() == FINISHED) {
            System.out.println();
        } else if (progress.getStage() == BGTObjectTableWriter.Stage.LOAD_OBJECTS) {

            String total = "";
            if (getTotalBytes() != null) {
                total = String.format(" - %s %4.1f%%   %s",
                        getBundleString("progress.total"),
                        100.0 / getTotalBytes() * getTotalBytesReadFunction().get(),
                        formatTimeSince(totalStart));
            }

            String current;
            if (getCurrentFileSize() != null) {
                current = String.format("%4.1f%% ", 100.0 / getCurrentFileSize() * progress.getBytesRead());
            } else {
                current = String.format("%,6d MB", progress.getBytesRead() / 1024 / 1024);
            }
            System.out.printf("\r%s: %s  %s, %,10d %s%s",
                    getCurrentFileName(),
                    current,
                    formatTimeSince(getCurrentFileStart()),
                    progress.getObjectCount(),
                    getBundleString("progress.objects"),
                    total
            );
        }
    }
}
