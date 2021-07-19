package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.OneToManyColumnMapping;
import nl.b3p.brmo.sql.dialect.SQLDialect;

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
import static nl.b3p.brmo.bgt.loader.BGTSchema.objectTypeAttributes;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;

public class BGTSchemaMapper {

    private static final String SCHEMA_VERSION_VALUE = "1";

    public enum Metadata {
        SCHEMA_VERSION,
        LOADER_VERSION,
        FEATURE_TYPES,
        INCLUDE_HISTORY,
        LINEARIZE_CURVES,
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

    private static Set<String> reservedWords = Stream.of(new String[]{
            "function"
    }).collect(Collectors.toSet());

    static {
        // Put lowercase version in mapping if not already mapped to another name
        getAllObjectTypes().forEach(name -> {
            if (!objectTypeNameToDutchTableName.containsKey(name)) {
                objectTypeNameToDutchTableName.put(name, name.toLowerCase());
            }
        });
        objectTypeNameToDutchTableName = Collections.unmodifiableMap(objectTypeNameToDutchTableName);
    }

    public static void printSchema(SQLDialect dialect, Predicate<String> typeNameFilter) {
        // Sort object type names by table names
        SortedMap<String,String> tableNamesObjectTypes = new TreeMap<>(getAllObjectTypes().stream()
                .filter(typeNameFilter == null ? s -> true : typeNameFilter)
                .collect(Collectors.toMap(BGTSchemaMapper::getTableNameForObjectType, typeName -> typeName)));

        String createTable = tableNamesObjectTypes.values().stream()
                .flatMap(typeName -> getCreateTableStatements(typeName, dialect))
                .collect(Collectors.joining("; \n\n"));
        System.out.println(createTable + ";\n\n");

        String geometryMetadata = tableNamesObjectTypes.values().stream()
                .flatMap(s -> getCreateGeometryMetadataStatements(s, dialect))
                .filter(sql -> sql.length() > 0)
                .collect(Collectors.joining(";\n"));
        if (geometryMetadata.length() > 0) {
            System.out.printf("-- %s\n\n", getBundleString("schema.geometry_metadata"));
            System.out.println(geometryMetadata + ";\n");
        }

        System.out.printf("-- %s\n\n", getBundleString("schema.loader_metadata"));
        System.out.println(getCreateMetadataTableStatements(dialect).collect(Collectors.joining(";\n")) + ";\n");

        System.out.printf("-- %s %s\n\n", getBundleString("schema.primary_keys"), getBundleString("schema.after_initial_load"));
        String primaryKeys = tableNamesObjectTypes.values().stream()
                .flatMap(typeName -> getCreatePrimaryKeyStatements(typeName, dialect))
                .collect(Collectors.joining("; \n"));
        System.out.println(primaryKeys + ";\n");

        System.out.printf("-- %s %s\n\n", getBundleString("schema.geometry_indexes"), getBundleString("schema.after_initial_load"));
        String geometryIndexes = tableNamesObjectTypes.values().stream()
                .flatMap(typeName -> getCreateGeometryIndexStatements(typeName, dialect))
                .collect(Collectors.joining(";\n"));
        System.out.println(geometryIndexes + ";\n");

    }

    public static String getTableNameForObjectType(String objectTypeName) {
        String tableName = objectTypeName;
        if (objectTypeNameToDutchTableName.containsKey(objectTypeName)) {
            tableName = objectTypeNameToDutchTableName.get(objectTypeName);
        }
        return tableName;
    }

    public static String getColumnNameForObjectType(String objectTypeName, String attributeName) {
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
        String tableNameLower = getTableNameForObjectType(objectTypeName).toLowerCase();
        String attributeNameLower = attributeName.toLowerCase();
        int i = attributeNameLower.indexOf(tableNameLower);
        if (i != -1) {
            attributeName = new StringBuilder(attributeName).replace(i, i + tableNameLower.length(), "").toString();
        }
        return attributeName.replaceAll("\\-", "_");
    }

    public static Stream<String> getOneToManyNames(String forTypeName) {
        return objectTypeAttributes.get(forTypeName).stream()
                .filter(column -> column instanceof OneToManyColumnMapping)
                .map(AttributeColumnMapping::getName);
    }

    public static Stream<String> getCreateTableStatements(String name, SQLDialect dialect) {
        List<String> statements = new ArrayList<>();
        String tableName = getTableNameForObjectType(name);
        if (dialect.supportsDropTableIfExists()) {
            statements.add("drop table if exists " + tableName);
        }
        String columns = objectTypeAttributes.get(name).stream()
                .filter(column -> !(column instanceof OneToManyColumnMapping))
                .map(column -> String.format("  %s %s%s",
                        getColumnNameForObjectType(name, column.getName()),
                        dialect.getType(column.getType()),
                        column.isNotNull() ? " not null" : ""))
                .collect(Collectors.joining(",\n"));
        statements.add(String.format("create table %s (\n%s\n)", tableName, columns));
        statements.addAll(getOneToManyNames(name)
                .flatMap(oneToManyName -> getCreateTableStatements(oneToManyName, dialect))
                .collect(Collectors.toList()));
        return statements.stream();
    }

    public static Stream<String> getCreatePrimaryKeyStatements(String name, SQLDialect dialect) {
        return getCreatePrimaryKeyStatements(name, dialect, true);
    }

    public static Stream<String> getCreatePrimaryKeyStatements(String name, SQLDialect dialect, boolean includeOneToMany) {
        String tableName = getTableNameForObjectType(name);
        String columns = objectTypeAttributes.get(name).stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .map(column -> getColumnNameForObjectType(name, column.getName()))
                .collect(Collectors.joining(", "));
        String sql = String.format("alter table %s add constraint %s_pkey primary key(%s)",
                tableName, tableName, columns);
        return Stream.concat(
                Stream.of(sql),
                includeOneToMany ? getOneToManyNames(name).flatMap(oneToManyName -> getCreatePrimaryKeyStatements(oneToManyName, dialect)) : Stream.empty()
        );
    }

    private static Stream<AttributeColumnMapping> geometryAttributeColumnMappings(String name) {
        return objectTypeAttributes.get(name).stream().filter(column -> column instanceof GeometryAttributeColumnMapping);
    }

    public static Stream<String> getCreateGeometryMetadataStatements(String name, SQLDialect dialect) {
        return Stream.concat(
                geometryAttributeColumnMappings(name).map(column -> dialect.getCreateGeometryMetadataSQL(
                        getTableNameForObjectType(name),
                        getColumnNameForObjectType(name, column.getName()),
                        column.getType())),
                getOneToManyNames(name).flatMap(oneToManyName -> getCreateGeometryMetadataStatements(oneToManyName, dialect))
        );
    }

    public static Stream<String> getCreateGeometryIndexStatements(String name, SQLDialect dialect) {
        return getCreateGeometryIndexStatements(name, dialect, true);
    }

    public static Stream<String> getCreateGeometryIndexStatements(String name, SQLDialect dialect, boolean includeOneToMany) {
        return Stream.concat(
                geometryAttributeColumnMappings(name).map(column ->dialect.getCreateGeometryIndexSQL(
                        getTableNameForObjectType(name),
                        getColumnNameForObjectType(name, column.getName()),
                        column.getType())),
                includeOneToMany ? getOneToManyNames(name).flatMap(oneToManyName -> getCreateGeometryIndexStatements(oneToManyName, dialect)) : Stream.empty()
        );
    }

    public static Stream<String> getCreateMetadataTableStatements(SQLDialect dialect) {
        List<String> statements = new ArrayList<>();
        final String tableName = "metadata";
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
        }).collect(Collectors.toMap(entry -> (Metadata)entry[0], entry -> (String)entry[1]));
        Stream.of(Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            statements.add(String.format("insert into %s (id, value) values ('%s', %s)", tableName, metadata.getDbKey(), value == null ? "null" : "'" + value + "'"));
        });

        return statements.stream();
    }
}
