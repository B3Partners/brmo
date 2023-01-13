/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.sql.dialect;

import nl.b3p.brmo.util.StandardLinearizedWKTWriter;

import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostGISDialect implements SQLDialect {
    private final StandardLinearizedWKTWriter wktWriter = new StandardLinearizedWKTWriter();
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public String getType(String type) {
        Matcher sequence = Pattern.compile("sequence\\((.+)\\)").matcher(type);
        if (sequence.matches()) {
            return "integer default nextval('" + sequence.group(1) + "')";
        }
        return type;
    }

    @Override
    public Object getGeometryParameter(Connection c, Geometry geometry, boolean linearizeCurves)
            throws SQLException {
        if (geometry == null) {
            return null;
        } else {
            String ewkt = getEWkt(geometry, linearizeCurves);
            PGobject object = new PGobject();
            object.setType("geometry");
            object.setValue(ewkt);
            return object;
        }
    }

    public String getEWkt(Geometry geometry, boolean linearizeCurves) {
        String wkt = linearizeCurves ? wktWriter.write(geometry) : wktWriter2.write(geometry);
        return "SRID=" + geometry.getSRID() + ";" + wkt;
    }

    @Override
    public String getCreateGeometryIndexSQL(String tableName, String geometryColumn, String type) {
        return String.format(
                "create index idx_%s_%s on %s using gist (%s)",
                tableName, geometryColumn, tableName, geometryColumn);
    }

    @Override
    public int getDefaultOptimalBatchSize() {
        return 2500;
    }
}
