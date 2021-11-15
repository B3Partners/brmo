/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2Object;

import javax.xml.stream.Location;

public class BAG2ToevoegingMutatie extends BAG2Mutatie {
    private BAG2Object toevoeging;

    BAG2ToevoegingMutatie(Location location, BAG2Object toevoeging) {
        super(location);
        this.toevoeging = toevoeging;
    }

    public BAG2Object getToevoeging() {
        return toevoeging;
    }

    @Override
    public String toString() {
        return "BAG2ToevoegingMutatie{" +
                "location=" + getLocation() +
                ", toevoeging=" + toevoeging +
                '}';
    }
}
