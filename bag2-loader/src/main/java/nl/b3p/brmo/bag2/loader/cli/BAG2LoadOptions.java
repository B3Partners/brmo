/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import picocli.CommandLine;

public class BAG2LoadOptions {
    @CommandLine.Option(names="--no-create-schema", negatable = true)
    boolean noCreateSchema;

    @CommandLine.Option(names="--max-objects", paramLabel= "<number>", hidden = true)
    Integer maxObjects;

    @CommandLine.Option(names={"--no-multithreading"}, negatable = true, hidden = true)
    boolean multithreading = true;

    boolean ignoreDuplicates = false;

    public boolean isNoCreateSchema() {
        return noCreateSchema;
    }

    public void setNoCreateSchema(boolean noCreateSchema) {
        this.noCreateSchema = noCreateSchema;
    }

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

    public boolean isIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public void setIgnoreDuplicates(boolean ignoreDuplicates) {
        this.ignoreDuplicates = ignoreDuplicates;
    }
}
