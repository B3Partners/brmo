/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import javax.xml.stream.Location;

public class BAG2Mutatie {
    private Location location;

    BAG2Mutatie(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
