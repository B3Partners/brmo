/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.SchemaSQLMapper;

public class BAG2SchemaMapper extends SchemaSQLMapper {
    private static BAG2SchemaMapper instance;

    public BAG2SchemaMapper() {
        super(BAG2Schema.getInstance());
    }

    public static BAG2SchemaMapper getInstance() {
        if (instance == null) {
            instance = new BAG2SchemaMapper();
        }
        return instance;
    }

    @Override
    public String getMetadataTableName() {
        return null;
    }
}
