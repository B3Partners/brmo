package nl.b3p.brmo.loader.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.brmo.loader.jdbc.ColumnMetadata;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 * create insert statement with geometry columns
 * 
 * @author Matthijs Laan
 */
public class TableRowInserter {
    private static final Log log = LogFactory.getLog(TableRowInserter.class);

    private final GeometryJdbcConverter geomToJdbc;
    
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    
    private final Connection c;
    private final String schema;
    
    private final boolean useSavepoints;

    private Savepoint recentSavepoint = null;
    
    private final DatabaseMetaData dbMetadata;
    
    /* Map van lowercase tabelnaam naar originele case tabelnaam */
    private final Map<String,String> tables = new HashMap();
    
    private final Map<String,SortedSet<ColumnMetadata>> tableColumns = new HashMap();
    
    public TableRowInserter(Connection c, GeometryJdbcConverter geomToJdbc, boolean useSavepoints, String schema) throws SQLException {
        this.c = c;
        this.geomToJdbc = geomToJdbc;
        this.schema = schema;
        this.useSavepoints = useSavepoints;
        
        dbMetadata = c.getMetaData();
        
        dbMetadata.getDatabaseProductName();
        log.debug(String.format("Database metadata: product %s, version %s, driver %s, version %s",
                dbMetadata.getDatabaseProductName(), dbMetadata.getDatabaseProductVersion(),
                dbMetadata.getDriverName(), dbMetadata.getDriverVersion()));
        
        ResultSet tablesRs = dbMetadata.getTables("", schema, "%", new String[] {"TABLE"});
        while(tablesRs.next()) {
            tables.put(tablesRs.getString("TABLE_NAME").toLowerCase(), tablesRs.getString("TABLE_NAME"));
        }
        tablesRs.close();
        log.debug(String.format("Tables found for schema %s: %d, list: %s",
                schema,
                tables.size(),
                tables.toString()));
    }
    
    public void insertRows(List<TableRow> rows) throws Exception {
        
        if(c.getAutoCommit()) {
            c.setAutoCommit(false);
        }
        
        for(TableRow row: rows) {
            try {
                insertRow(row);
            } catch(Exception e) {

                try {
                    c.rollback();
                } catch(SQLException rbe) {
                    log.error("Error rolling back transaction", rbe);
                }

                throw new Exception(String.format("Error inserting row into %s (%s) values (%s)", row.getTable(),
                        row.getColumns().toString(), row.getValues().toString()), e);
            }
        }
        if(recentSavepoint != null) {
            c.releaseSavepoint(recentSavepoint);
        }
        c.commit();        
    }
    
    private SortedSet<ColumnMetadata> getTableColumnMetadata(String table) throws SQLException {
        
        table = table.toLowerCase();
        
        if(!tables.containsKey(table)) {
            throw new IllegalArgumentException("Table does not exist in schema " + schema + ": " + table);
        }

        log.debug("Getting column metadata for table " + table);
        
        SortedSet<ColumnMetadata> columnMetadata = (SortedSet<ColumnMetadata>)tableColumns.get(table);
        if(columnMetadata == null) {
            columnMetadata = new TreeSet();
            tableColumns.put(table, columnMetadata);
            
            ResultSet columnsRs = dbMetadata.getColumns(null, schema, tables.get(table), "%");
            while(columnsRs.next()) {
                ColumnMetadata cm = new ColumnMetadata(columnsRs);
                columnMetadata.add(cm);
            }
            for(ColumnMetadata cm: columnMetadata) {
                log.debug("  " + cm.toStringFixedWidth(columnMetadata));
            }
            columnsRs.close();
        }
        return columnMetadata;
    }
    
    private ColumnMetadata findColumnMetadata(SortedSet<ColumnMetadata> tableColumnMetadata, String columnName) {
        for(ColumnMetadata cm: tableColumnMetadata) {
            if(cm.getName().toLowerCase().equals(columnName.toLowerCase())) {
                return cm;
            }
        }
        return null;
    }
    
    public void insertRow(TableRow row) throws Exception {
        
        if(useSavepoints) {
            if(row.isIgnoreDuplicates()) {
                if(recentSavepoint == null) {
                    recentSavepoint = c.setSavepoint();
                    log.debug("Created savepoint with id: " + recentSavepoint.getSavepointId());
                } else {
                    log.debug("No need for new savepoint, previous insert caused rollback to recent savepoint with id " + recentSavepoint.getSavepointId());
                }
            } else if(recentSavepoint != null) {
                log.debug("About to insert non-recoverable row, discarding savepoint with id " + recentSavepoint.getSavepointId());
                c.releaseSavepoint(recentSavepoint);
                recentSavepoint = null;
            }
        }
        
        StringBuilder sql = new StringBuilder("insert into ");
        
        sql.append(row.getTable());
        
        SortedSet<ColumnMetadata> tableColumnMetadata = getTableColumnMetadata(row.getTable());
        
        // TODO maak sql op basis van alle columns en gebruik null values
        // zodat insert voor elke tabel hetzelfde, reuse preparedstatement
        
        sql.append(" (");
        StringBuilder valuesSql  = new StringBuilder(") values (");
        List params = new ArrayList();
        List<Boolean> isGeometry = new ArrayList();
        
        Iterator<String> valuesIt = row.getValues().iterator();
        for(Iterator<String> it = row.getColumns().iterator(); it.hasNext();) {
            String column = it.next();
            String stringValue = valuesIt.next();
            
            ColumnMetadata cm = findColumnMetadata(tableColumnMetadata, column);
            
            if(cm == null) {
                throw new IllegalArgumentException("Column not found: " + column + " in table " + row.getTable());
            } 
            
            String insertValueSql = "?";
            
            boolean isThisGeometry = false;
            Object param = null;
            if(stringValue != null) {
                stringValue = stringValue.trim();
                switch(cm.getDataType()) {
                    case java.sql.Types.DECIMAL:
                    case java.sql.Types.NUMERIC:
                    case java.sql.Types.INTEGER:
                        try {
                            param = new BigDecimal(stringValue);
                        } catch(NumberFormatException nfe) {
                            log.error(String.format("Cannot convert value \"%s\" to type %s for %s.%s",
                                    stringValue,
                                    cm.getTypeName(),
                                    row.getTable(),
                                    cm.getName()));
                            //param = -99999;
                        }
                        break;
                    case java.sql.Types.CHAR:
                    case java.sql.Types.VARCHAR:
                        param = stringValue;
                        break;
                    case java.sql.Types.OTHER:
                        if(cm.getTypeName().equals("SDO_GEOMETRY") || cm.getTypeName().equals("geometry")) {
                            param = stringValue;
                            isThisGeometry = true;
                            break;
                        } else {
                            throw new IllegalStateException(String.format("Column \"%s\" (value to insert \"%s\") type other but not geometry!", column, param));
                        }
                    case java.sql.Types.DATE:
                    case java.sql.Types.TIMESTAMP:
                        param = javax.xml.bind.DatatypeConverter.parseDateTime(stringValue);
                        if (param!=null){
                            Calendar cal = (Calendar)param;
                            param = new java.sql.Date(cal.getTimeInMillis());
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Data type %s (#%d) of column \"%s\" not supported", cm.getTypeName(), cm.getDataType(), column));
                }
            } else {
                isThisGeometry = cm.getTypeName().equals("SDO_GEOMETRY");
            }
            params.add(param);
            isGeometry.add(isThisGeometry);
            
            sql.append(cm.getName());
            valuesSql.append(insertValueSql);
            
            if(it.hasNext()) {
                sql.append(", ");
                valuesSql.append(", ");
            }
        }
        sql.append(valuesSql);
        sql.append(")");

        log.debug("SQL: " + sql);

        PreparedStatement stmt = null;
        try {
            stmt = c.prepareStatement(sql.toString());
            for(int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if(Boolean.TRUE.equals(isGeometry.get(i))) {
                    //System.out.println("Geometry param: " + param);
                    
                    if(geomToJdbc.convertsGeometryInsteadOfWkt()) {
                        WKTReader reader = new WKTReader(geometryFactory);
                        Geometry geom = param == null || ((String)param).trim().length() == 0 ? null : reader.read((String)param);
                        stmt.setObject(i+1, geomToJdbc.convertGeometry(geom));
                    } else {
                        stmt.setObject(i+1, geomToJdbc.convertWkt((String)param));
                    }
                } else {
                    stmt.setObject(i+1, param);
                }
            }
            stmt.executeUpdate();
            
            if(recentSavepoint != null) {
                log.debug("Releasing savepoint with id " + recentSavepoint.getSavepointId());
                c.releaseSavepoint(recentSavepoint);
                recentSavepoint = null;
            }
        } catch(Exception e) {
            if(e instanceof SQLException) {
                String message = ((SQLException)e).getMessage();
                if(message.startsWith("ORA-00001:") || message.startsWith("ERROR: duplicate key value violates unique constraint")) {
                    if(row.isIgnoreDuplicates()) {
                        
                        if(recentSavepoint != null) {
                            log.debug("Rolling back to savepoint with id " + recentSavepoint.getSavepointId());
                            c.rollback(recentSavepoint);
                        }
                        
                        return;
                    }
                }
            }
            
            log.error("Error executing statement: " + sql);
            log.error("                   params: " + params.toString());
            log.error("        ignore duplicates: " + row.isIgnoreDuplicates());

            throw e;
        } finally {
            DbUtils.closeQuietly(stmt);
        }
    }    
}
