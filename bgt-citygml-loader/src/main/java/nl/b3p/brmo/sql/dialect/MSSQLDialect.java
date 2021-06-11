package nl.b3p.brmo.sql.dialect;

import com.microsoft.sqlserver.jdbc.Geometry;
import org.geotools.geometry.jts.MultiSurface;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class MSSQLDialect implements SQLDialect {
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

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
    public Object getGeometryParameter(org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        String wkt;
        if (linearizeCurves) {
            wkt = geometry.toString();
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
            return Geometry.STGeomFromText(wkt, 28992);
        } catch(SQLException e) {
            throw new SQLException("Error converting geometry to MSSQL geometry for WKT: " + wkt, e);
        }
    }


    @Override
    public void setGeometryParameter(PreparedStatement ps, int parameterIndex, int pmdType, org.locationtech.jts.geom.Geometry geometry, boolean linearizeCurves) throws SQLException {
        if (geometry == null) {
            // Note that using the pmdType (-157 for GEOMETRY) does not work (using mssql-jdbc 9.2.1.jre8), throwing
            // com.microsoft.sqlserver.jdbc.SQLServerException: The conversion from OBJECT to GEOMETRY is unsupported.
            ps.setNull(parameterIndex, Types.OTHER);
        } else {
            ps.setObject(parameterIndex, getGeometryParameter(geometry, linearizeCurves));
        }
    }
}
