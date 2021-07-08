package nl.b3p.brmo.imgeo.cli;

import picocli.CommandLine.Option;

public class ExtractSelectionOptions extends FeatureTypeSelectionOptions {
    @Option(names={"--geo-filter"}, paramLabel="\"<wkt>\"")
    String geoFilterWkt;
}
