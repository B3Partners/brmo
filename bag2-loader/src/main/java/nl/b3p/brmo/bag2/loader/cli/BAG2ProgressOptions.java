/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import nl.b3p.brmo.bgt.loader.ProgressReporter;
import picocli.CommandLine;

public class BAG2ProgressOptions {

    private ProgressReporter customProgressReporter;

    @CommandLine.Option(names="--progress", hidden = true)
    private boolean progress;

    public boolean isConsoleProgressEnabled() {
        if (isProgress()) {
            return true;
        }
        // Imperfect but good enough without using native library like Jansi's isatty()
        // https://stackoverflow.com/questions/1403772/how-can-i-check-if-a-java-programs-input-output-streams-are-connected-to-a-term
        return System.console() != null;
    }

    public boolean isProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }

    public ProgressReporter getCustomProgressReporter() {
        return customProgressReporter;
    }

    public void setCustomProgressReporter(ProgressReporter customProgressReporter) {
        this.customProgressReporter = customProgressReporter;
    }
}
