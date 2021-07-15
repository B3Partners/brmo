package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.BGTSchema.getAllObjectTypes;
import static nl.b3p.brmo.bgt.loader.BGTSchema.objectTypeAttributes;

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

        StringBuilder geometryMetadata = new StringBuilder();
        StringBuilder geometryIndexes = new StringBuilder();
        tableNamesObjectTypes.forEach((tableName, typeName) -> {
            System.out.println(createTable(typeName, dialect));
            createGeometryMetadataAndIndexes(typeName, dialect, geometryMetadata, geometryIndexes);
            objectTypeAttributes.get(typeName).stream().filter(a -> a instanceof OneToManyColumnMapping).forEach(oneToMany -> {
                System.out.println(createTable(oneToMany.getName(), dialect));
                createGeometryMetadataAndIndexes(oneToMany.getName(), dialect, geometryMetadata, geometryIndexes);
            });
        });
        if (geometryMetadata.length() > 0) {
            System.out.println("-- Geometry metadata\n");
            System.out.println(geometryMetadata);
        }
        System.out.println("-- Geometry indexen (pas aanmaken na inladen stand)\n");
        System.out.println(geometryIndexes);

        System.out.println("-- Loader metadata\n");
        System.out.println(createMetadataTable(dialect));
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
            sql.append("drop table if exists ");
            sql.append(tableName);
            sql.append(";\n");
        }
        sql.append("create table ");
        sql.append(tableName);
        sql.append(" (\n");
        AtomicBoolean first = new AtomicBoolean(true);
        List<String> primaryKeys = new ArrayList<>();
        objectTypeAttributes.get(name).forEach(column -> {
            if (!(column instanceof OneToManyColumnMapping)) {
                String columnName = getColumnNameForObjectType(name, column.getName());
                column.appendToCreateTableSql(sql, dialect, columnName, first);
                if (column.isPrimaryKey()) {
                    primaryKeys.add(columnName);
                }
            }
        });
        sql.append(",\n  primary key(").append(String.join(", ", primaryKeys));
        sql.append(")\n);\n");
        return sql.toString();
    }

    public static void createGeometryMetadataAndIndexes(String name, SQLDialect dialect, StringBuilder metadata, StringBuilder indexes) {
        String tableName = getTableNameForObjectType(name);
        objectTypeAttributes.get(name).forEach(column -> {
            if (column instanceof GeometryAttributeColumnMapping) {
                String columnName = getColumnNameForObjectType(name, column.getName());

                String s = dialect.getCreateGeometryMetadataSQL(tableName, columnName, column.getType());
                if (s.length() > 0) {
                    metadata.append(s).append("\n");
                }

                s = dialect.getCreateGeometryIndexSQL(tableName, columnName, column.getType());
                if (s.length() > 0) {
                    indexes.append(s).append("\n");
                }
            }
        });
    }

    public static String createMetadataTable(SQLDialect dialect) {
        final StringBuilder sql = new StringBuilder();
        final String tableName = "metadata";
        if (dialect.supportsDropTableIfExists()) {
            sql.append("drop table if exists ");
            sql.append(tableName);
            sql.append(";\n");
        }
        sql.append("create table ");
        sql.append(tableName);
        sql.append(" (\n");
        sql.append("  id ").append(dialect.getType("varchar(255)")).append(",\n");
        sql.append("  value ").append(dialect.getType("text")).append(",\n");
        sql.append("  primary key(id)\n);\n");
        Map<Metadata,String> defaultMetadata = Stream.of(new Object[][]{
                {Metadata.SCHEMA_VERSION, SCHEMA_VERSION_VALUE},
                {Metadata.LOADER_VERSION, getLoaderVersion()},
        }).collect(Collectors.toMap(entry -> (Metadata)entry[0], entry -> (String)entry[1]));
        Stream.of(Metadata.values()).forEach(metadata -> {
            String value = defaultMetadata.get(metadata);
            sql.append(String.format("insert into %s (id, value) values ('%s', %s);\n", tableName, metadata.getDbKey(), value == null ? "null" : "'" + value + "'"));
        });

        return sql.toString();
    }

    public static String getLoaderVersion() {
        return ResourceBundle.getBundle("BGTCityGMLLoader").getString("app.version");
    }
}
