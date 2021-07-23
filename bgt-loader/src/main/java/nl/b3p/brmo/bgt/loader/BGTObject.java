/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.bgt.loader;

import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;

import javax.xml.stream.Location;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static nl.b3p.brmo.bgt.loader.BGTObject.MutatieStatus.*;

public class BGTObject {
    private final BGTSchema.BGTObjectType objectType;
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

    public BGTObject(BGTSchema.BGTObjectType objectType, Map<String,Object> attributes) {
        this.objectType = objectType;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.xmlLocation = null;
        this.mutatieStatus = WORDT;
        this.mutatiePreviousVersionGmlId = null;
    }

    public BGTObject(BGTSchema.BGTObjectType objectType, Map<String,Object> attributes, Location xmlLocation) {
        this.objectType = objectType;
        this.attributes = attributes;
        this.xmlLocation = xmlLocation;
        this.mutatieStatus = WORDT;
        this.mutatiePreviousVersionGmlId = null;
    }

    public BGTObject(BGTSchema.BGTObjectType objectType, Map<String,Object> attributes, Location xmlLocation, MutatieStatus mutatieStatus, String mutatiePreviousVersionGmlId) {
        this.objectType = objectType;
        this.attributes = attributes;
        this.xmlLocation = xmlLocation;
        this.mutatieStatus = mutatieStatus;
        this.mutatiePreviousVersionGmlId = mutatiePreviousVersionGmlId;
    }

    public BGTSchema.BGTObjectType getObjectType() {
        return objectType;
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
                ? String.format("%s: %s", objectType.getName(), s)
                : String.format("%s at xml line %6d: %s", objectType.getName(), xmlLocation.getLineNumber(), s);
    }
}
