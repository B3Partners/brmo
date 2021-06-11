package nl.b3p.brmo.sql.dialect;

import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class PostGISDialect implements SQLDialect{
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    @Override
    public Object getGeometryParameter(Geometry geometry, boolean linearizeCurves) throws SQLException {
        if(geometry == null) {
            return null;
        } else {
            final String wkt = linearizeCurves ? geometry.toString() : wktWriter2.write(geometry);
            PGobject object = new PGobject();
            object.setType("geometry");
            object.setValue("SRID=28992;" + wkt);
            return object;
        }
    }
}
