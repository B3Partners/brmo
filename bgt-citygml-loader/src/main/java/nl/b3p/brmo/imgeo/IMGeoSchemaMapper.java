package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.OneToManyColumnMapping;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.imgeo.IMGeoSchema.getAllObjectTypes;
import static nl.b3p.brmo.imgeo.IMGeoSchema.getIMGeoPlusObjectTypes;
import static nl.b3p.brmo.imgeo.IMGeoSchema.getOnlyBGTObjectTypes;
import static nl.b3p.brmo.imgeo.IMGeoSchema.objectTypeAttributes;

public class IMGeoSchemaMapper {

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

    // map column names -> snake case, voor sld, voor oracle, mssql

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            System.err.println("Error: expected zero or one argument");
            System.exit(1);
        }
//        SQLDialect dialect = new OracleDialect(null);
        SQLDialect dialect = new MSSQLDialect();
        Set<String> objectTypes = getAllObjectTypes();
        // TODO gebruik library voor command line opties
        if (args.length == 1) {
            switch(args[0]) {
                // TODO metadata tabellen
                case "--bgt":
                    objectTypes = getOnlyBGTObjectTypes();
                    break;
                case "--plus":
                    objectTypes = getIMGeoPlusObjectTypes();
                    break;
                case "--all":
                    objectTypes = getAllObjectTypes();
                    break;
                default:
                    System.err.println("Error: invalid argument, expected --bgt, --plus or --all");
                    System.exit(1);
            }
        }

        objectTypes.stream().sorted().forEach(name -> {
            System.out.println(createTable(name, dialect));
        });
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
            sql.append("drop table if exists \"");
            sql.append(tableName);
            sql.append("\";\n");
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
}
