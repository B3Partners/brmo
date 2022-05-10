/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.loader.cli;

import picocli.CommandLine;

public class NHRLoadOptions {
    @CommandLine.Option(names="--location", description = "Location of the NHR service")
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @CommandLine.Option(names = "--preprod", description = "Set to true if targeting preproduction.")
    private boolean preprod;

    public boolean getPreprod() {
        return preprod;
    }

    public void setPreprod(boolean preprod) {
        this.preprod = preprod;
    }
}
