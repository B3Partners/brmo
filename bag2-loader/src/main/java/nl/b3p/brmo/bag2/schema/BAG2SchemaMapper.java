/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.bag2.loader.BAG2LoaderUtils;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BAG2SchemaMapper extends SchemaSQLMapper {
    private static BAG2SchemaMapper instance;

    private static final String SCHEMA_VERSION_VALUE = "1";

    public static final String METADATA_TABLE_NAME = "brmo_metadata";

    public enum Metadata {
        SCHEMA_VERSION,
        LOADER_VERSION,
        STAND_LOAD_TIME,
        STAND_LOAD_TECHNISCHE_DATUM,
        CURRENT_TECHNISCHE_DATUM,
        GEMEENTE_CODES,
        FILTER_MUTATIES_WOONPLAATS;

        public String getDbKey() {
            return "bag2_" + this.name().toLowerCase();
        }
    }

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
        return METADATA_TABLE_NAME;
    }

    public List<String> getCreateMetadataTableStatements(SQLDialect dialect, String tablePrefix, boolean dropIfExists) {
        List<String> statements = super.getCreateMetadataTableStatements(dialect, tablePrefix, dropIfExists);

        Map<BAG2SchemaMapper.Metadata, String> defaultMetadata = Stream.of(new Object[][]{
                {BAG2SchemaMapper.Metadata.SCHEMA_VERSION, SCHEMA_VERSION_VALUE},
                {BAG2SchemaMapper.Metadata.LOADER_VERSION, BAG2LoaderUtils.getLoaderVersion()},
        }).collect(Collectors.toMap(entry -> (BAG2SchemaMapper.Metadata) entry[0], entry -> (String) entry[1]));

        Stream.of(BAG2SchemaMapper.Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            statements.add(String.format("insert into %s (naam, waarde) values ('%s', %s)",
                    getMetadataTableName(),
                    metadata.getDbKey(),
                    value == null ? "null" : "'" + value + "'"));
        });

        return statements;
    }
}
