/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

public class Force2DCoordinateSequenceFactory implements CoordinateSequenceFactory {
    @Override
    public CoordinateSequence create(Coordinate[] coordinates) {
        if (coordinates != null) {
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = new CoordinateXY(coordinates[i].getX(), coordinates[i].getY());
            }
        }
        return new CoordinateArraySequence(coordinates);
    }

    @Override
    public CoordinateSequence create(CoordinateSequence coordSeq) {
        return create(coordSeq.toCoordinateArray());
    }

    @Override
    public CoordinateSequence create(int size, int dimension) {
        return new CoordinateArraySequence(size, 2);
    }
}
