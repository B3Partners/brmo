package nl.b3p.brmo.sql;

public class GeometryAttributeColumnMapping extends AttributeColumnMapping {

    public GeometryAttributeColumnMapping(String name, String type) {
        super(name, type, false, false);
    }

    public GeometryAttributeColumnMapping(String name) {
        this(name, "geometry(GEOMETRY, 28992)");
    }

    @Override
    public Object toQueryParameter(Object value)  {
        return (org.locationtech.jts.geom.Geometry) value;
    }
}