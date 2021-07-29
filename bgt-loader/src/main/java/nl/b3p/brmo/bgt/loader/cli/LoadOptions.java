/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class LoadOptions {
    @Option(names={"--no-create-schema"}, negatable = true)
    boolean createSchema = true;

    @Option(names={"--include-history"})
    boolean includeHistory = false;

    @Option(names="--table-prefix", defaultValue = "", hidden = true)
    String tablePrefix = "";

    @Option(names="--max-objects", paramLabel= "<number>", hidden = true)
    Integer maxObjects;

    @Option(names={"--linearize-curves"})
    boolean linearizeCurves = false;

    @Option(names={"--no-multithreading"}, negatable = true, hidden = true)
    boolean multithreading = true;

    public boolean isCreateSchema() {
        return createSchema;
    }

    public void setCreateSchema(boolean createSchema) {
        this.createSchema = createSchema;
    }

    public boolean isIncludeHistory() {
        return includeHistory;
    }

    public void setIncludeHistory(boolean includeHistory) {
        this.includeHistory = includeHistory;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public Integer getMaxObjects() {
        return maxObjects;
    }

    public void setMaxObjects(Integer maxObjects) {
        this.maxObjects = maxObjects;
    }

    public boolean isLinearizeCurves() {
        return linearizeCurves;
    }

    public void setLinearizeCurves(boolean linearizeCurves) {
        this.linearizeCurves = linearizeCurves;
    }

    public boolean isMultithreading() {
        return multithreading;
    }

    public void setMultithreading(boolean multithreading) {
        this.multithreading = multithreading;
    }
}
