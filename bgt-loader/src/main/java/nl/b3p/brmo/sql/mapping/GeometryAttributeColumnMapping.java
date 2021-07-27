/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.sql.mapping;

import org.locationtech.jts.geom.Geometry;

public class GeometryAttributeColumnMapping extends AttributeColumnMapping {

    public GeometryAttributeColumnMapping(String name, String type) {
        super(name, type, false, false);
    }

    public GeometryAttributeColumnMapping(String name) {
        this(name, "geometry(GEOMETRY, 28992)");
    }

    @Override
    public Geometry toQueryParameter(Object value)  {
        return (Geometry) value;
    }
}
