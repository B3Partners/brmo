package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class LoadOptions {
    @Option(names={"--include-history"})
    boolean includeHistory = false;

    @Option(names="--max-objects", paramLabel= "<number>")
    Integer maxObjects;

    @Option(names={"--linearize-curves"})
    boolean linearizeCurves = false;
}
