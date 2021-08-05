/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class CLIOptions {

    @Option(names="--progress", hidden = true)
    boolean progress;

    public boolean isConsoleProgressEnabled() {
        if (progress) {
            return true;
        }
        // Imperfect but good enough without using native library like Jansi's isatty()
        // https://stackoverflow.com/questions/1403772/how-can-i-check-if-a-java-programs-input-output-streams-are-connected-to-a-term
        return System.console() != null;
    }
}
