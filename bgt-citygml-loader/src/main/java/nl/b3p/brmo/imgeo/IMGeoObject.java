package nl.b3p.brmo.imgeo;

import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;

import javax.xml.stream.Location;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static nl.b3p.brmo.imgeo.IMGeoObject.MutatieStatus.*;

public class IMGeoObject {
    private final String name;
    private final Map<String,Object> attributes;
    private final Location xmlLocation;
    private final MutatieStatus mutatieStatus;
    private final String mutatiePreviousVersionGmlId;

    enum MutatieStatus {
        WORDT,
        WAS_WORDT,
        WAS
    }

    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    public IMGeoObject(String name, Map<String,Object> attributes) {
        this.name = name;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.xmlLocation = null;
        this.mutatieStatus = WORDT;
        this.mutatiePreviousVersionGmlId = null;
    }

    public IMGeoObject(String name, Map<String,Object> attributes, Location xmlLocation) {
        this.name = name;
        this.attributes = attributes;
        this.xmlLocation = xmlLocation;
        this.mutatieStatus = WORDT;
        this.mutatiePreviousVersionGmlId = null;
    }

    public IMGeoObject(String name, Map<String,Object> attributes, Location xmlLocation, MutatieStatus mutatieStatus, String mutatiePreviousVersionGmlId) {
        this.name = name;
        this.attributes = attributes;
        this.xmlLocation = xmlLocation;
        this.mutatieStatus = mutatieStatus;
        this.mutatiePreviousVersionGmlId = mutatiePreviousVersionGmlId;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Location getXmlLocation() {
        return xmlLocation;
    }

    public MutatieStatus getMutatieStatus() {
        return mutatieStatus;
    }

    public String getMutatiePreviousVersionGmlId() {
        return mutatiePreviousVersionGmlId;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("{");
        SortedSet<String> attributeNames = new TreeSet<>(attributes.keySet());
        boolean first = true;
        for(String name: attributeNames) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            s.append(name);
            s.append("=");
            Object value = attributes.get(name);
            if (value instanceof Geometry) {
                value = wktWriter2.write((Geometry)value);
            }
            s.append(value);
        }
        s.append("}");
        return xmlLocation == null
                ? String.format("%s: %s", name, s)
                : String.format("%s at xml line %6d: %s", name, xmlLocation.getLineNumber(), s);
    }
}
