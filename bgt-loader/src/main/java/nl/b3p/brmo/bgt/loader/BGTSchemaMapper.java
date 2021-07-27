/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.sql.mapping.AttributeColumnMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.BGTSchema.getAllObjectTypes;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;

public class BGTSchemaMapper {

    private static final String SCHEMA_VERSION_VALUE = "1";

    public static final String METADATA_TABLE = "bgt_metadata";

    public enum Metadata {
        SCHEMA_VERSION,
        LOADER_VERSION,
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

    public static Map<String, String> objectTypeNameToDutchTableName = Stream.of(new String[][]{
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

    private static final Set<String> reservedWords = Stream.of(new String[]{
            "function"
    }).collect(Collectors.toSet());

    /**
     * Use for aligning fixed width output.
     */
    public static final int MAX_TABLE_LENGTH;

    static {
        // Put lowercase version in mapping if not already mapped to another name
        getAllObjectTypes()
                .map(BGTSchema.BGTObjectType::getName)
                .filter(name -> !objectTypeNameToDutchTableName.containsKey(name))
                .forEach(name -> objectTypeNameToDutchTableName.put(name, name.toLowerCase()));
        objectTypeNameToDutchTableName = Collections.unmodifiableMap(objectTypeNameToDutchTableName);

        MAX_TABLE_LENGTH = objectTypeNameToDutchTableName.values().stream().map(String::length).reduce(0, Integer::max);
    }

    public static void printSchema(SQLDialect dialect, String tablePrefix, Predicate<BGTSchema.BGTObjectType> objectTypeFilter) {
        // Sort object type names by table names
        SortedMap<String, BGTSchema.BGTObjectType> tableNamesObjectTypes = new TreeMap<>(getAllObjectTypes()
                .filter(objectTypeFilter == null ? objectType -> true : objectTypeFilter)
                .collect(Collectors.toMap(objectType -> BGTSchemaMapper.getTableNameForObjectType(objectType, ""), objectType -> objectType)));

        String createTable = tableNamesObjectTypes.values().stream()
                .flatMap(objectType -> getCreateTableStatements(objectType, dialect, tablePrefix))
                .collect(Collectors.joining("; \n\n"));
        System.out.println(createTable + ";\n\n");

        String geometryMetadata = tableNamesObjectTypes.values().stream()
                .flatMap(objectType -> getCreateGeometryMetadataStatements(objectType, dialect, tablePrefix))
                .filter(sql -> sql.length() > 0)
                .collect(Collectors.joining(";\n"));
        if (geometryMetadata.length() > 0) {
            System.out.printf("-- %s\n\n", getBundleString("schema.geometry_metadata"));
            System.out.println(geometryMetadata + ";\n");
        }

        System.out.printf("-- %s\n\n", getBundleString("schema.loader_metadata"));
        System.out.println(getCreateMetadataTableStatements(dialect, tablePrefix).collect(Collectors.joining(";\n")) + ";\n");

        System.out.printf("-- %s %s\n\n", getBundleString("schema.primary_keys"), getBundleString("schema.after_initial_load"));
        String primaryKeys = tableNamesObjectTypes.values().stream()
                .flatMap(objectType -> getCreatePrimaryKeyStatements(objectType, dialect, tablePrefix))
                .collect(Collectors.joining("; \n"));
        System.out.println(primaryKeys + ";\n");

        System.out.printf("-- %s %s\n\n", getBundleString("schema.geometry_indexes"), getBundleString("schema.after_initial_load"));
        String geometryIndexes = tableNamesObjectTypes.values().stream()
                .flatMap(objectType -> getCreateGeometryIndexStatements(objectType, dialect, tablePrefix))
                .collect(Collectors.joining(";\n"));
        System.out.println(geometryIndexes + ";\n");

    }

    public static String getTableNameForObjectType(BGTSchema.BGTObjectType objectType, String tablePrefix) {
        return tablePrefix + objectTypeNameToDutchTableName.get(objectType.getName());
    }

    public static String getColumnNameForObjectType(BGTSchema.BGTObjectType objectType, String attributeName) {
        attributeName = attributeName.toLowerCase();
        if (attributeName.startsWith("geometrie")) {
            return "geom";
        }
        if (attributeName.startsWith("kruinlijn")) {
            return "geom_kruinlijn";
        }
        if (reservedWords.contains(attributeName)) {
            attributeName = attributeName + "_";
        }
        String tableNameLower = getTableNameForObjectType(objectType, null).toLowerCase();
        String attributeNameLower = attributeName.toLowerCase();
        int i = attributeNameLower.indexOf(tableNameLower);
        if (i != -1) {
            attributeName = new StringBuilder(attributeName).replace(i, i + tableNameLower.length(), "").toString();
        }
        return attributeName.replaceAll("\\-", "_");
    }

    public static Stream<String> getCreateTableStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix) {
        List<String> statements = new ArrayList<>();
        String tableName = getTableNameForObjectType(objectType, tablePrefix);
        if (dialect.supportsDropTableIfExists()) {
            statements.add("drop table if exists " + tableName);
        }
        String columns = objectType.getDirectAttributes().stream()
                .map(column -> String.format("  %s %s%s",
                        getColumnNameForObjectType(objectType, column.getName()),
                        dialect.getType(column.getType()),
                        column.isNotNull() ? " not null" : ""))
                .collect(Collectors.joining(",\n"));
        statements.add(String.format("create table %s (\n%s\n)", tableName, columns));
        statements.addAll(objectType.getOneToManyAttributeObjectTypes().stream()
                .flatMap(oneToManyObjectType -> getCreateTableStatements(oneToManyObjectType, dialect, tablePrefix))
                .collect(Collectors.toList()));
        return statements.stream();
    }

    public static Stream<String> getCreatePrimaryKeyStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return getCreatePrimaryKeyStatements(objectType, dialect, tablePrefix, true);
    }

    public static Stream<String> getCreatePrimaryKeyStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix, boolean includeOneToMany) {
        String tableName = getTableNameForObjectType(objectType, tablePrefix);
        String columns = objectType.getDirectAttributes().stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .map(column -> getColumnNameForObjectType(objectType, column.getName()))
                .collect(Collectors.joining(", "));
        String sql = String.format("alter table %s add constraint %s_pkey primary key(%s)",
                tableName, tableName, columns);
        return Stream.concat(
                Stream.of(sql),
                includeOneToMany
                        ? objectType.getOneToManyAttributeObjectTypes().stream().flatMap(oneToManyObjectType -> getCreatePrimaryKeyStatements(oneToManyObjectType, dialect, tablePrefix))
                        : Stream.empty()
        );
    }

    public static Stream<String> getCreateGeometryMetadataStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return Stream.concat(
                objectType.getGeometryAttributes().stream().map(column -> dialect.getCreateGeometryMetadataSQL(
                        getTableNameForObjectType(objectType, tablePrefix),
                        getColumnNameForObjectType(objectType, column.getName()),
                        column.getType())),
                objectType.getOneToManyAttributeObjectTypes().stream()
                        .flatMap(oneToManyObjectType -> getCreateGeometryMetadataStatements(oneToManyObjectType, dialect, tablePrefix))
        );
    }

    public static Stream<String> getCreateGeometryIndexStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return getCreateGeometryIndexStatements(objectType, dialect, tablePrefix, true);
    }

    public static Stream<String> getCreateGeometryIndexStatements(BGTSchema.BGTObjectType objectType, SQLDialect dialect, String tablePrefix, boolean includeOneToMany) {
        return Stream.concat(
                objectType.getGeometryAttributes().stream().map(column ->dialect.getCreateGeometryIndexSQL(
                        getTableNameForObjectType(objectType, tablePrefix),
                        getColumnNameForObjectType(objectType, column.getName()),
                        column.getType())),
                includeOneToMany
                        ? objectType.getOneToManyAttributeObjectTypes().stream().flatMap(oneToManyObjectType -> getCreateGeometryIndexStatements(oneToManyObjectType, dialect, tablePrefix))
                        : Stream.empty()
        );
    }

    public static Stream<String> getCreateMetadataTableStatements(SQLDialect dialect, String tablePrefix) {
        List<String> statements = new ArrayList<>();
        final String tableName = METADATA_TABLE;
        if (dialect.supportsDropTableIfExists()) {
            statements.add("drop table if exists " + tableName);
        }
        String sql = "create table " + tableName + " (\n" +
                "  id " + dialect.getType("varchar(255)") + ",\n" +
                "  value " + dialect.getType("text") + ",\n" +
                "  primary key(id)\n)";
        statements.add(sql);
        Map<Metadata,String> defaultMetadata = Stream.of(new Object[][]{
                {Metadata.SCHEMA_VERSION, SCHEMA_VERSION_VALUE},
                {Metadata.LOADER_VERSION, Utils.getLoaderVersion()},
                {Metadata.TABLE_PREFIX, tablePrefix},
        }).collect(Collectors.toMap(entry -> (Metadata)entry[0], entry -> (String)entry[1]));
        Stream.of(Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            statements.add(String.format("insert into %s (id, value) values ('%s', %s)", tableName, metadata.getDbKey(), value == null ? "null" : "'" + value + "'"));
        });

        return statements.stream();
    }
}
