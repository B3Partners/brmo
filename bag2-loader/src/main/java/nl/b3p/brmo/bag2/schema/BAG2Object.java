/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.SchemaObjectInstance;

import java.util.Map;

public class BAG2Object extends SchemaObjectInstance {
    private final MutatieStatus mutatieStatus;

    private BAG2Object wijzigingWas;
    private BAG2Object wijzigingWordt;

    public enum MutatieStatus {
        WIJZIGING_WAS,
        WIJZIGING_WORDT,
        TOEVOEGING,
    }

    public BAG2Object(BAG2ObjectType objectType, Map<String, Object> attributes, MutatieStatus mutatieStatus) {
        this(objectType, attributes, mutatieStatus, null, null);
    }

    public BAG2Object(BAG2ObjectType objectType, Map<String, Object> attributes, MutatieStatus mutatieStatus, BAG2Object wijzigingWas, BAG2Object wijzigingWordt) {
        super(objectType, attributes);
        this.mutatieStatus = mutatieStatus;
        this.wijzigingWas = wijzigingWas;
        this.wijzigingWordt = wijzigingWordt;
    }

    public BAG2ObjectType getObjectType() {
        return (BAG2ObjectType) super.getObjectType();
    }

    public MutatieStatus getMutatieStatus() {
        return mutatieStatus;
    }

    public BAG2Object getWijzigingWas() {
        return wijzigingWas;
    }

    public BAG2Object getWijzigingWordt() {
        return wijzigingWordt;
    }

    public void setWijzigingWas(BAG2Object wijzigingWas) {
        this.wijzigingWas = wijzigingWas;
    }

    public void setWijzigingWordt(BAG2Object wijzigingWordt) {
        this.wijzigingWordt = wijzigingWordt;
    }
}
