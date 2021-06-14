package nl.b3p.brmo.sql.dialect;

import nl.b3p.brmo.util.StandardLinearizedWKTWriter;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class PostGISDialect implements SQLDialect{
    private final StandardLinearizedWKTWriter wktWriter = new StandardLinearizedWKTWriter();
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    @Override
    public Object getGeometryParameter(Geometry geometry, boolean linearizeCurves) throws SQLException {
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
}
