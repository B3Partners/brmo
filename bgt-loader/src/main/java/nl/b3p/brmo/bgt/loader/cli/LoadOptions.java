package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class LoadOptions {
    @Option(names={"--no-create-schema"}, negatable = true)
    boolean createSchema = true;

    @Option(names={"--include-history"})
    boolean includeHistory = false;

    @Option(names="--table-prefix", defaultValue = "", hidden = true)
    String tablePrefix;

    @Option(names="--max-objects", paramLabel= "<number>")
    Integer maxObjects;

    @Option(names={"--linearize-curves"})
    boolean linearizeCurves = false;

    @Option(names={"--no-multithreading"}, negatable = true)
    boolean multithreading = true;

    public boolean isCreateSchema() {
        return createSchema;
    }

    public boolean isIncludeHistory() {
        return includeHistory;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Integer getMaxObjects() {
        return maxObjects;
    }

    public boolean isLinearizeCurves() {
        return linearizeCurves;
    }

    public boolean isMultithreading() {
        return multithreading;
    }
}
