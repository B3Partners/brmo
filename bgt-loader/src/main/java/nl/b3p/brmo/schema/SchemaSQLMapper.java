/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.schema.mapping.ArrayAttributeMapping;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;

public abstract class SchemaSQLMapper {
    private final Schema schema;

    protected final Map<String,String> objectTypeNameToTableName = new HashMap<>();

    private static final Set<String> reservedWords = Stream.of(new String[]{
            "function"
    }).collect(Collectors.toSet());

    public SchemaSQLMapper(Schema schema) {
        this.schema = schema;
        schema.getAllObjectTypes()
                .map(ObjectType::getName)
                .forEach(name -> objectTypeNameToTableName.put(name, name.toLowerCase()));
    }

    public Schema getSchema() {
        return schema;
    }

    public abstract String getMetadataTableName();

    public String getTableNameForObjectType(ObjectType objectType, String tablePrefix) {
        return tablePrefix + objectTypeNameToTableName.get(objectType.getName());
    }

    public String getTableNameForArrayAttribute(ObjectType objectType, ArrayAttributeMapping arrayAttribute, String tablePrefix) {
        return getTableNameForObjectType(objectType, tablePrefix) + "_" + arrayAttribute.getTableSuffix();
    }

    public String getColumnNameForObjectType(ObjectType objectType, String attributeName) {
        attributeName = attributeName.toLowerCase();
        if (reservedWords.contains(attributeName)) {
            attributeName = attributeName + "_";
        }
        String tableNameLower = getTableNameForObjectType(objectType, null).toLowerCase();
        String attributeNameLower = attributeName.toLowerCase();
        int i = attributeNameLower.indexOf(tableNameLower);
        if (i != -1) {
            attributeName = new StringBuilder(attributeName).replace(i, i + tableNameLower.length(), "").toString();
        }
        return attributeName.replaceAll("-", "_");
    }

    public void printSchema(SQLDialect dialect, String tablePrefix, Predicate<ObjectType> objectTypeFilter) {
        // Sort object type names by table names
        SortedMap<String, ObjectType> tableNamesObjectTypes = new TreeMap<>(getSchema().getAllObjectTypes()
                .filter(objectTypeFilter == null ? objectType -> true : objectTypeFilter)
                .collect(Collectors.toMap(objectType -> getTableNameForObjectType(objectType, ""), objectType -> objectType)));

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
        System.out.println(String.join(";\n", getCreateMetadataTableStatements(dialect, tablePrefix)) + ";\n");

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

    public Stream<String> getCreateTableStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix) {
        List<String> statements = new ArrayList<>();

        // Drop and create referencing tables first
        statements.addAll(objectType.getAllAttributes().stream()
                .filter(attribute -> attribute instanceof ArrayAttributeMapping)
                .flatMap(arrayAttribute -> getArrayAttributeCreateTableStatements(objectType, (ArrayAttributeMapping) arrayAttribute, dialect, tablePrefix))
                .collect(Collectors.toList()));

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

    public Stream<String> getArrayAttributeCreateTableStatements(ObjectType objectType, ArrayAttributeMapping arrayAttribute, SQLDialect dialect, String tablePrefix) {
        List<String> statements = new ArrayList<>();
        String tableName = getTableNameForArrayAttribute(objectType, arrayAttribute, tablePrefix);
        if (dialect.supportsDropTableIfExists()) {
            statements.add("drop table if exists " + tableName);
        }
        String columns = Stream.concat(
                    objectType.getDirectAttributes().stream().filter(AttributeColumnMapping::isPrimaryKey),
                    Stream.of(arrayAttribute))
                .map(column -> String.format("  %s %s%s",
                        getColumnNameForObjectType(objectType, column.getName()),
                        dialect.getType(column.getType()),
                        column.isNotNull() ? " not null" : ""))
                .collect(Collectors.joining(",\n"));
        statements.add(String.format("create table %s (\n%s\n)", tableName, columns));
        // Afterwards: foreign keys (object type PK columns)
        // Unordered list, no primary key
        return statements.stream();
    }

    public Stream<String> getCreatePrimaryKeyStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return getCreatePrimaryKeyStatements(objectType, dialect, tablePrefix, true);
    }

    public Stream<String> getCreatePrimaryKeyStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix, boolean includeOneToMany) {
        String tableName = getTableNameForObjectType(objectType, tablePrefix);
        String columns = objectType.getDirectAttributes().stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .map(column -> getColumnNameForObjectType(objectType, column.getName()))
                .collect(Collectors.joining(", "));
        Stream<String> tablePrimaryKey;
        if (columns.length() > 0) {
            String sql = String.format("alter table %s add constraint %s_pkey primary key(%s)",
                    tableName, tableName, columns);
            tablePrimaryKey = Stream.of(sql);
        } else {
            tablePrimaryKey = Stream.empty();
        }
        return Stream.concat(
                tablePrimaryKey,
                includeOneToMany
                        ? objectType.getOneToManyAttributeObjectTypes().stream().flatMap(oneToManyObjectType -> getCreatePrimaryKeyStatements(oneToManyObjectType, dialect, tablePrefix))
                        : Stream.empty()
        );
    }

    public Stream<String> getCreateGeometryMetadataStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return Stream.concat(
                objectType.getGeometryAttributes().stream().map(column -> dialect.getCreateGeometryMetadataSQL(
                        getTableNameForObjectType(objectType, tablePrefix),
                        getColumnNameForObjectType(objectType, column.getName()),
                        column.getType())),
                objectType.getOneToManyAttributeObjectTypes().stream()
                        .flatMap(oneToManyObjectType -> getCreateGeometryMetadataStatements(oneToManyObjectType, dialect, tablePrefix))
        );
    }

    public Stream<String> getCreateGeometryIndexStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix) {
        return getCreateGeometryIndexStatements(objectType, dialect, tablePrefix, true);
    }

    public Stream<String> getCreateGeometryIndexStatements(ObjectType objectType, SQLDialect dialect, String tablePrefix, boolean includeOneToMany) {
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

    public List<String> getCreateMetadataTableStatements(SQLDialect dialect, String tablePrefix) {
        List<String> statements = new ArrayList<>();
        final String tableName = getMetadataTableName();
        if (dialect.supportsDropTableIfExists()) {
            statements.add("drop table if exists " + tableName);
        }
        String sql = String.format("create table %s(\n  naam %s,\n  waarde %s,\n  primary key(naam)\n)",
                tableName,
                dialect.getType("varchar(255)"),
                dialect.getType("text"));
        statements.add(sql);

        return statements;
    }

    /**
     * Use for aligning fixed width output.
     */
    public int getMaxTableLength() {
        return objectTypeNameToTableName.values().stream().map(String::length).reduce(0, Integer::max);
    }
}
