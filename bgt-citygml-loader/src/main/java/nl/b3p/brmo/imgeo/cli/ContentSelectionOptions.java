package nl.b3p.brmo.imgeo.cli;

import picocli.CommandLine.Option;

import java.util.List;

public class ContentSelectionOptions {
    @Option(names={"--feature-types"}, split=",", defaultValue="all", paramLabel = "<name>")
    List<String> featureTypes;

    @Option(names={"--geo-filter"}, paramLabel="<wkt>")
    String geoFilterWkt;
}
