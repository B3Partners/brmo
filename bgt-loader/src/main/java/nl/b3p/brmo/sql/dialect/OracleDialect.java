package nl.b3p.brmo.sql.dialect;

import oracle.jdbc.OracleConnection;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleDialect implements SQLDialect {

    public OracleDialect() {
    }

    @Override
    public void loadDriver() throws ClassNotFoundException {
        Class.forName("oracle.jdbc.OracleDriver");
    }

    @Override
    public boolean supportsDropTableIfExists() {
        return false;
    }

    @Override
    public String getType(String type) {
        Matcher varchar = Pattern.compile("varchar\\((.+)\\)").matcher(type);
        if(varchar.matches()) {
            return "varchar2(" + varchar.group(1) + " char)";
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
        if(type.equals("text")) {
            return "clob";
        }
        return type;
    }

    @Override
    public Object getGeometryParameter(Connection c, org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        OracleConnection connection = c.unwrap(OracleConnection.class);
        return new GeometryConverter(connection).toSDO(geometry);
    }

    @Override
    public void setGeometryParameter(Connection c, PreparedStatement ps, int parameterIndex, int pmdType, Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null || geometry.isEmpty()) {
            ps.setNull(parameterIndex, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(c, geometry, linearizeCurves));
        }
    }

    @Override
    public String getCreateGeometryMetadataSQL(String tableName, String geometryColumn, String type) {
        return String.format("insert into user_sdo_geom_metadata values ('%S', '%S', MDSYS.SDO_DIM_ARRAY(" +
                "MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), " +
                "MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1) " +
                "), 28992);",
                tableName, geometryColumn);
    }

    @Override
    public String getCreateGeometryIndexSQL(String tableName, String geometryColumn, String type) {
        return String.format("create index idx_%s_%s on %s (%s) indextype is mdsys.spatial_index;",
                tableName, geometryColumn, tableName, geometryColumn);
    }

    @Override
    public int getDefaultOptimalBatchSize() {
        return 500;
    }
}
