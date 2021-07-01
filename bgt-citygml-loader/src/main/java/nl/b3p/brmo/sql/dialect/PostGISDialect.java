package nl.b3p.brmo.sql.dialect;

import nl.b3p.brmo.util.StandardLinearizedWKTWriter;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;

public class PostGISDialect implements SQLDialect{
    private final StandardLinearizedWKTWriter wktWriter = new StandardLinearizedWKTWriter();
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    @Override
    public void loadDriver() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
    }

    @Override
    public Object getGeometryParameter(Connection c, Geometry geometry, boolean linearizeCurves) throws SQLException {
        if(geometry == null) {
            return null;
        } else {
            String wkt = linearizeCurves ? wktWriter.write(geometry) : wktWriter2.write(geometry);
            PGobject object = new PGobject();
            object.setType("geometry");
            object.setValue("SRID=" + geometry.getSRID() + ";" + wkt);
            return object;
        }
    }

    public String getCreateGeometryIndex(String tableName, String geometryColumn, String type) {
        return String.format("create index idx_%s_%s on %s using gist (%s);",
            tableName, geometryColumn, tableName, geometryColumn);
    }

    @Override
    public int getDefaultOptimalBatchSize() {
        return 2500;
    }
}
