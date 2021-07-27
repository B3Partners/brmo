/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class ExtractSelectionOptions extends FeatureTypeSelectionOptions {
    @Option(names={"--geo-filter"}, paramLabel="\"<wkt>\"")
    String geoFilterWkt;

    public String getGeoFilterWkt() {
        return geoFilterWkt;
    }

    public void setGeoFilterWkt(String geoFilterWkt) {
        this.geoFilterWkt = geoFilterWkt;
    }
}
