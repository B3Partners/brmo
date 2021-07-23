/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.sql.dialect;

import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SQLDialect {
    void loadDriver() throws ClassNotFoundException;

    default String getType(String type) {
        return type;
    }

    default boolean supportsDropTableIfExists() {
        return true;
    }

    default Object getGeometryParameter(Connection c, Geometry geometry) throws SQLException {
        return getGeometryParameter(c, geometry, false);
    }

    Object getGeometryParameter(Connection c, Geometry geometry, boolean linearizeCurves) throws SQLException;

    default void setGeometryParameter(Connection c, PreparedStatement ps, int parameterIndex, int pmdType, Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null) {
            ps.setNull(parameterIndex, pmdType);
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(c, geometry, linearizeCurves));
        }
    }

    default String getCreateGeometryMetadataSQL(String tableName, String geometryColumn, String type) {
        return "";
    }

    default String getCreateGeometryIndexSQL(String tableName, String geometryColumn, String type) {
        return "";
    }

    int getDefaultOptimalBatchSize();
}
