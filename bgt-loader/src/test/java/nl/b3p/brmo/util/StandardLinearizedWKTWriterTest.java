/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.util;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardLinearizedWKTWriterTest {
    StandardLinearizedWKTWriter writer = new StandardLinearizedWKTWriter();

    @Test
    void write() {
        GeometryFactory gf = new GeometryFactory();
        assertEquals("POINT (0 0)", writer.write(gf.createPoint(new Coordinate(0, 0))));
        assertEquals("LINESTRING (0 0, 1 1, 0 0)", writer.write(gf.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 0),
        })));
    }
}