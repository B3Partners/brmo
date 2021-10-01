/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import picocli.CommandLine;

public class BAG2LoadOptions {
    @CommandLine.Option(names="--max-objects", paramLabel= "<number>", hidden = true)
    Integer maxObjects;

    @CommandLine.Option(names={"--no-multithreading"}, negatable = true, hidden = true)
    boolean multithreading = true;

    public Integer getMaxObjects() {
        return maxObjects;
    }

    public void setMaxObjects(Integer maxObjects) {
        this.maxObjects = maxObjects;
    }

    public boolean isMultithreading() {
        return multithreading;
    }

    public void setMultithreading(boolean multithreading) {
        this.multithreading = multithreading;
    }
}
