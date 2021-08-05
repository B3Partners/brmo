/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.schema;

import nl.b3p.brmo.schema.SchemaObjectInstance;

import javax.xml.stream.Location;
import java.util.Map;

import static nl.b3p.brmo.bgt.schema.BGTObject.MutatieStatus.WORDT;

public class BGTObject extends SchemaObjectInstance {
    private final Location xmlLocation;
    private final MutatieStatus mutatieStatus;
    private final String mutatiePreviousVersionGmlId;

    public enum MutatieStatus {
        WORDT,
        WAS_WORDT,
        WAS
    }

    public BGTObject(BGTObjectType objectType, Map<String, Object> attributes) {
        this(objectType, attributes, null);
    }

    public BGTObject(BGTObjectType objectType, Map<String, Object> attributes, Location xmlLocation) {
        this(objectType, attributes, xmlLocation, WORDT, null);
    }

    public BGTObject(BGTObjectType objectType, Map<String, Object> attributes, Location xmlLocation, MutatieStatus mutatieStatus, String mutatiePreviousVersionGmlId) {
        super(objectType, attributes);
        this.xmlLocation = xmlLocation;
        this.mutatieStatus = mutatieStatus;
        this.mutatiePreviousVersionGmlId = mutatiePreviousVersionGmlId;
    }

    public BGTObjectType getObjectType() {
        return (BGTObjectType) super.getObjectType();
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
        String s = super.toString();
        return xmlLocation == null ? s : String.format("xml line %6d: %s", xmlLocation.getLineNumber(), s);
    }
}
