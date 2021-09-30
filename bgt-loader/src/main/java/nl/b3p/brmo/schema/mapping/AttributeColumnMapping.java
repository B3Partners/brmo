/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

public class AttributeColumnMapping {
    private final String name;
    private final String type;
    private final boolean notNull;
    private final boolean primaryKey;
    private final boolean useDefault;

    public AttributeColumnMapping(String name, String type, boolean notNull, boolean primaryKey, boolean useDefault) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.primaryKey = primaryKey;
        this.useDefault = useDefault;
    }

    public AttributeColumnMapping(String name, String type, boolean notNull, boolean primaryKey) {
        this(name, type, notNull, primaryKey, false);
    }

    public AttributeColumnMapping(String name, String type, boolean notNull) {
        this(name, type, notNull, false, false);
    }

    public AttributeColumnMapping(String name) {
        this(name, "varchar(255)", true, false);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public boolean isDirectInsertAttribute() {
        return !isUseDefault();
    }

    public Object toQueryParameter(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
