/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.schema;

import nl.b3p.brmo.bgt.loader.Utils;
import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BGTSchemaMapper extends SchemaSQLMapper {
    public static BGTSchemaMapper instance;

    private static final String SCHEMA_VERSION_VALUE = "1";

    public static final String METADATA_TABLE_NAME = "brmo_metadata";

    public enum Metadata {
        SCHEMA_VERSION,
        LOADER_VERSION,
        BRMOVERSIE,
        FEATURE_TYPES,
        INCLUDE_HISTORY,
        LINEARIZE_CURVES,
        TABLE_PREFIX,
        INITIAL_LOAD_TIME,
        INITIAL_LOAD_DELTA_ID,
        DELTA_ID,
        DELTA_TIME_TO,
        GEOM_FILTER;

        public String getDbKey() {
            return this.name().toLowerCase();
        }
    }

    public static Map<String, String> bgtObjectTypeTableNames = Stream.of(new String[][]{
            {"PlantCover", "begroeidterreindeel"},
            {"BuildingInstallation", "gebouwinstallatie"},
            {"AuxiliaryTrafficArea", "ondersteunendwegdeel"},
            {"BridgeConstructionElement", "overbruggingsdeel"},
            {"BuildingPart", "pand"},
            {"Railway", "spoor"},
            {"TunnelPart", "tunneldeel"},
            {"SolitaryVegetationObject", "vegetatieobject"},
            {"TrafficArea", "wegdeel"}
    }).collect(Collectors.toMap(e -> e[0], e -> e[1]));


    public BGTSchemaMapper() {
        super(BGTSchema.getInstance());
        bgtObjectTypeTableNames.forEach((typeName, table) -> objectTypeNameToTableName.put(typeName, table));
    }

    public static BGTSchemaMapper getInstance() {
        if (instance == null) {
            instance = new BGTSchemaMapper();
        }
        return instance;
    }

    @Override
    public String getMetadataTableName() {
        return METADATA_TABLE_NAME;
    }

    public String getColumnNameForObjectType(ObjectType objectType, String attributeName) {
        if (attributeName.startsWith("geometrie")) {
            return "geom";
        }
        if (attributeName.startsWith("kruinlijn")) {
            return "geom_kruinlijn";
        }

        return super.getColumnNameForObjectType(objectType, attributeName);
    }

    public List<String> getCreateMetadataTableStatements(SQLDialect dialect, String tablePrefix) {
       List<String> statements = super.getCreateMetadataTableStatements(dialect, tablePrefix);

        Map<BGTSchemaMapper.Metadata, String> defaultMetadata = Stream.of(new Object[][]{
                {BGTSchemaMapper.Metadata.SCHEMA_VERSION, SCHEMA_VERSION_VALUE},
                {BGTSchemaMapper.Metadata.LOADER_VERSION, Utils.getLoaderVersion()},
                {BGTSchemaMapper.Metadata.BRMOVERSIE, Utils.getBrmoVersion()},
                {BGTSchemaMapper.Metadata.TABLE_PREFIX, tablePrefix},
        }).collect(Collectors.toMap(entry -> (BGTSchemaMapper.Metadata) entry[0], entry -> (String) entry[1]));

        Stream.of(BGTSchemaMapper.Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            statements.add(String.format("insert into %s (naam, waarde) values ('%s', %s)",
                    getMetadataTableName(),
                    metadata.getDbKey(),
                    value == null ? "null" : "'" + value + "'"));
        });

        return statements;
    }
}
