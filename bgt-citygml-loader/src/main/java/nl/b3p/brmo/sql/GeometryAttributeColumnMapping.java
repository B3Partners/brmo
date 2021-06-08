package nl.b3p.brmo.sql;

import nl.b3p.brmo.PGGeometryString;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.util.PGobject;

public class GeometryAttributeColumnMapping extends AttributeColumnMapping {

    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    public GeometryAttributeColumnMapping(String name, String type) {
        super(name, type, false, false);
    }

    public GeometryAttributeColumnMapping(String name) {
        this(name, "geometry(GEOMETRY, 28992)");
    }

    @Override
    public Object toQueryParameter(Object value) throws Exception {
        if(value == null) {
            return null;
        } else {
            PGobject object = new PGobject();
            object.setType("geometry");
            object.setValue("SRID=28992;" + wktWriter2.write((Geometry)value));
            return object;
        }
    }
}
