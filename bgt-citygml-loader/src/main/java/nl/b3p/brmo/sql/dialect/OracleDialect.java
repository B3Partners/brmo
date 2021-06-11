package nl.b3p.brmo.sql.dialect;

import oracle.jdbc.OracleConnection;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OracleDialect implements SQLDialect {

    private final GeometryConverter geometryConverter;

    public OracleDialect(OracleConnection connection) {
        this.geometryConverter = new GeometryConverter(connection);
    }

    @Override
    public boolean supportsDropTableIfExists() {
        return false;
    }

    @Override
    public String getType(String type) {
        if(type.startsWith("varchar")) {
            return "varchar2" + type.substring("varchar".length());
        }
        if(type.equals("integer")) {
            return "number(10)";
        }
        if(type.equals("boolean")) {
            return "varchar2(5)";
        }
        if(type.startsWith("geometry(")) {
            return "MDSYS.SDO_GEOMETRY";
        }
        return type;
    }

    @Override
    public Object getGeometryParameter(org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        return geometryConverter.toSDO(geometry);
    }

    @Override
    public void setGeometryParameter(PreparedStatement ps, int parameterIndex, int pmdType, Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null || geometry.isEmpty()) {
            ps.setNull(parameterIndex, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(geometry, linearizeCurves));
        }
    }
}
