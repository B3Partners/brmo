/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema.mapping;

public class ForeignKeyAttributeMapping extends AttributeColumnMapping {
    private String referencing;

    public ForeignKeyAttributeMapping(String name, String referencing, String type, boolean notNull) {
        super(name, type, notNull, false);
        this.referencing = referencing;
    }

    public String getReferencing() {
        return referencing;
    }
}
