/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema.mapping;

public class ArrayAttributeMapping extends AttributeColumnMapping {
    private String tableSuffix;

    public ArrayAttributeMapping(String name, String tableSuffix, String type) {
        super(name, type, true, false);
        this.tableSuffix = tableSuffix;
    }

    @Override
    public boolean isDirectInsertAttribute() {
        return false;
    }

    public String getTableSuffix() {
        return tableSuffix;
    }
}
