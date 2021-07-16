package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.AttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.Collections;
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

    public static void printSchema(SQLDialect dialect, Predicate<String> typeNameFilter, boolean withKeysAndIndexes) {
        // Sort object type names by table names
        SortedMap<String,String> tableNamesObjectTypes = new TreeMap<>(getAllObjectTypes().stream()
                .filter(typeNameFilter == null ? s -> true : typeNameFilter)
                .collect(Collectors.toMap(BGTSchemaMapper::getTableNameForObjectType, typeName -> typeName)));

        StringBuilder geometryMetadata = new StringBuilder();
        StringBuilder geometryIndexes = new StringBuilder();
        StringBuilder primaryKeys = new StringBuilder();
        tableNamesObjectTypes.forEach((tableName, typeName) -> {
            System.out.println(createTable(typeName, dialect));
            primaryKeys.append(createPrimaryKey(typeName, dialect));
            createGeometryMetadataAndIndexes(typeName, dialect, geometryMetadata, geometryIndexes);
            objectTypeAttributes.get(typeName).stream().filter(a -> a instanceof OneToManyColumnMapping).forEach(oneToMany -> {
                System.out.println(createTable(oneToMany.getName(), dialect));
                primaryKeys.append(createPrimaryKey(oneToMany.getName(), dialect));
                createGeometryMetadataAndIndexes(oneToMany.getName(), dialect, geometryMetadata, geometryIndexes);
            });
        });
        if (geometryMetadata.length() > 0) {
            System.out.printf("-- %s\n\n", getBundleString("schema.geometry_metadata"));
            System.out.println(geometryMetadata);
        }

        System.out.printf("-- %s\n\n", getBundleString("schema.loader_metadata"));
        System.out.println(createMetadataTable(dialect));

        System.out.printf("-- %s %s\n\n", getBundleString("schema.primary_keys"), getBundleString("schema.after_initial_load"));
        System.out.println(primaryKeys);
        System.out.printf("-- %s %s\n\n", getBundleString("schema.geometry_indexes"), getBundleString("schema.after_initial_load"));
        System.out.println(geometryIndexes);

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

    public static String createTable(String name, SQLDialect dialect) {
        String tableName = getTableNameForObjectType(name);
        StringBuilder sql = new StringBuilder();
        if (dialect.supportsDropTableIfExists()) {
            sql.append("drop table if exists ").append(tableName).append(";\n");
        }
        sql.append("create table ").append(tableName).append(" (\n");
        String columns = objectTypeAttributes.get(name).stream()
                .filter(column -> !(column instanceof OneToManyColumnMapping))
                .map(column -> String.format("  %s %s%s",
                        getColumnNameForObjectType(name, column.getName()),
                        dialect.getType(column.getType()),
                        column.isNotNull() ? " not null" : ""))
                .collect(Collectors.joining(",\n"));
        sql.append(columns);
        sql.append(");\n");
        return sql.toString();
    }

    public static String createPrimaryKey(String name, SQLDialect dialect) {
        String tableName = getTableNameForObjectType(name);
        String columns = objectTypeAttributes.get(name).stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .map(column -> getColumnNameForObjectType(name, column.getName()))
                .collect(Collectors.joining(", "));
        return String.format("alter table %s add constraint %s_pkey primary key(%s);\n",
                tableName, tableName, columns);
    }

    public static void createGeometryMetadataAndIndexes(String name, SQLDialect dialect, StringBuilder metadata, StringBuilder indexes) {
        String tableName = getTableNameForObjectType(name);
        objectTypeAttributes.get(name).stream().filter(column -> column instanceof GeometryAttributeColumnMapping).forEach(column -> {
            String columnName = getColumnNameForObjectType(name, column.getName());

            String s = dialect.getCreateGeometryMetadataSQL(tableName, columnName, column.getType());
            if (s.length() > 0) {
                metadata.append(s).append("\n");
            }

            s = dialect.getCreateGeometryIndexSQL(tableName, columnName, column.getType());
            if (s.length() > 0) {
                indexes.append(s).append("\n");
            }
        });
    }

    public static String createMetadataTable(SQLDialect dialect) {
        final StringBuilder sql = new StringBuilder();
        final String tableName = "metadata";
        if (dialect.supportsDropTableIfExists()) {
            sql.append("drop table if exists ").append(tableName).append(";\n");
        }
        sql.append("create table ").append(tableName).append(" (\n");
        sql.append("  id ").append(dialect.getType("varchar(255)")).append(",\n");
        sql.append("  value ").append(dialect.getType("text")).append(",\n");
        sql.append("  primary key(id)\n);\n");
        Map<Metadata,String> defaultMetadata = Stream.of(new Object[][]{
                {Metadata.SCHEMA_VERSION, SCHEMA_VERSION_VALUE},
                {Metadata.LOADER_VERSION, Utils.getLoaderVersion()},
        }).collect(Collectors.toMap(entry -> (Metadata)entry[0], entry -> (String)entry[1]));
        Stream.of(Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            sql.append(String.format("insert into %s (id, value) values ('%s', %s);\n", tableName, metadata.getDbKey(), value == null ? "null" : "'" + value + "'"));
        });

        return sql.toString();
    }
}
