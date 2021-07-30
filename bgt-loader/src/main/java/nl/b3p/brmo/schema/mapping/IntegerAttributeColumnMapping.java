/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

public class IntegerAttributeColumnMapping extends AttributeColumnMapping {
    public IntegerAttributeColumnMapping(String name, boolean notNull, boolean primaryKey) {
        super(name, "integer", notNull, primaryKey);
    }

    public IntegerAttributeColumnMapping(String name, boolean notNull) {
        super(name, "integer", notNull, false);
    }

    public IntegerAttributeColumnMapping(String name) {
        this(name, true);
    }

    @Override
    public Object toQueryParameter(Object value) {
        if(value == null) {
            return null;
        } else {
            return Integer.parseInt(value.toString());
        }
    }
}
