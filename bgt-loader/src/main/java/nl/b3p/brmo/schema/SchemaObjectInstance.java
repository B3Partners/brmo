/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SchemaObjectInstance {
    private final ObjectType objectType;
    private final Map<String, Object> attributes;

    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    public SchemaObjectInstance(ObjectType objectType, Map<String, Object> attributes) {
        this.objectType = objectType;
        this.attributes = attributes;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(objectType.getName());
        s.append("{");
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
        return s.toString();
    }
}
