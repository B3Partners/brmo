/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.stream.Stream;

import static nl.b3p.brmo.bag2.schema.BAG2Schema.ARCHIVE_SUFFIX;

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

    @Override
    public Stream<String> getCreateTableStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix) {

        // Make both current and archive tables

        String originalTypeName = objectType.getName().endsWith(ARCHIVE_SUFFIX) ? objectType.getName().split(ARCHIVE_SUFFIX)[0] : objectType.getName();
        return Stream.concat(
                super.getCreateTableStatements(BAG2Schema.getInstance().getObjectTypeByName(originalTypeName), dialect, tablePrefix),
                super.getCreateTableStatements(BAG2Schema.getInstance().getObjectTypeByName(originalTypeName + ARCHIVE_SUFFIX), dialect, tablePrefix)
        );
    }
}
