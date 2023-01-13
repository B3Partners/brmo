/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

public class BooleanAttributeColumnMapping extends AttributeColumnMapping {
    public BooleanAttributeColumnMapping(String name, boolean notNull) {
        super(name, "boolean", notNull, false);
    }

    public BooleanAttributeColumnMapping(String name) {
        this(name, true);
    }

    @Override
    public Object toQueryParameter(Object value) {
        if (value == null) {
            return null;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }
}
