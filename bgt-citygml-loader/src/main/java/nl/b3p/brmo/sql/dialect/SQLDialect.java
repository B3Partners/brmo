package nl.b3p.brmo.sql.dialect;

import org.locationtech.jts.geom.Geometry;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SQLDialect {
    default String getType(String type) {
        return type;
    }

    default Object getGeometryParameter(Geometry geometry) throws SQLException {
        return getGeometryParameter(geometry, false);
    }

    // TODO
    //public void createSpatialIndex(String columnName, String type);

    default boolean supportsDropTableIfExists() {
        return true;
    }

    Object getGeometryParameter(Geometry geometry, boolean linearizeCurves) throws SQLException;

    default void setGeometryParameter(PreparedStatement ps, int parameterIndex, int pmdType, Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null) {
            ps.setNull(parameterIndex, pmdType);
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(geometry, linearizeCurves));
        }
    }

    default String getCreateGeometryMetadata(String tableName, String geometryColumn, String type) {
        return "";
    }

    default String getCreateGeometryIndex(String tableName, String geometryColumn, String type) {
        return "";
    }
}
