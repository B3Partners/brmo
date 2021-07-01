package nl.b3p.brmo.sql.dialect;

import com.microsoft.sqlserver.jdbc.Geometry;
import nl.b3p.brmo.util.StandardLinearizedWKTWriter;
import org.geotools.geometry.jts.MultiSurface;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class MSSQLDialect implements SQLDialect {
    private final StandardLinearizedWKTWriter wktWriter = new StandardLinearizedWKTWriter();
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    @Override
    public void loadDriver() throws ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Override
    public String getType(String type) {
        if(type.startsWith("varchar")) {
            return "n" + type;
        }
        if(type.equals("timestamp")) {
            return "datetime2";
        }
        if(type.equals("boolean")) {
            return "bit";
        }
        if(type.startsWith("geometry(")) {
            return "geometry";
        }
        return type;
    }

    @Override
    public Object getGeometryParameter(Connection c, org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        String wkt;
        if (linearizeCurves) {
            wkt = wktWriter.write(geometry);
        } else {
            if (geometry instanceof MultiSurface) {
                // MSSQL does not support MULTISURFACE but does support CURVEPOLYGON and CIRCULARSTRING. Convert the
                // geometry to a CURVEPOLYGON if there is only a single geometry component and otherwise a
                // GEOMETRYCOLLECTION, which is only subtly different from a MULTISURFACE.

                // If we had a WKB writer supporting curves we could use Geometry.STGeomFromWKB()

                MultiSurface multiSurface = (MultiSurface) geometry;
                if (multiSurface.getNumGeometries() == 1) {
                    wkt = wktWriter2.write(multiSurface.getGeometryN(0));
                } else {
                    GeometryFactory geometryFactory = new GeometryFactory();
                    org.locationtech.jts.geom.Geometry[] geometries = new org.locationtech.jts.geom.Geometry[multiSurface.getNumGeometries()];
                    for(int i = 0; i < multiSurface.getNumGeometries(); i++) {
                        geometries[i] = multiSurface.getGeometryN(i);
                    }
                    GeometryCollection collection = geometryFactory.createGeometryCollection(geometries);
                    wkt = wktWriter2.write(collection);
//                        // Convert after conversion to WKT to avoid using a GeometryFactory
//                        wkt = wktWriter2.write(multiSurface);
//                        wkt = "GEOMETRYCOLLECTION" + wkt.substring("MULTISURFACE".length());
                }
            } else {
                wkt = wktWriter2.write(geometry);
            }
        }
        try {
            return Geometry.STGeomFromText(wkt, geometry.getSRID());
        } catch(SQLException e) {
            throw new SQLException("Error converting geometry to MSSQL geometry for WKT: " + wkt, e);
        }
    }


    @Override
    public void setGeometryParameter(Connection c, PreparedStatement ps, int parameterIndex, int pmdType, org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null) {
            // Note that using the pmdType (-157 for GEOMETRY) does not work (using mssql-jdbc 9.2.1.jre8), throwing
            // com.microsoft.sqlserver.jdbc.SQLServerException: The conversion from OBJECT to GEOMETRY is unsupported.
            ps.setNull(parameterIndex, Types.OTHER);
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(c, geometry, linearizeCurves));
        }
    }

    @Override
    public String getCreateGeometryIndex(String tableName, String geometryColumn, String type) {
        return String.format("create spatial index idx_%s_%s on %s (%s) with (bounding_box = (12000,304000,280000,620000));",
                tableName, geometryColumn, tableName, geometryColumn);
    }

    @Override
    public int getDefaultOptimalBatchSize() {
        return 250;
    }
}
