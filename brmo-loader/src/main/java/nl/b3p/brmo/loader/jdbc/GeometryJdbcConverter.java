
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.io.ParseException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author Matthijs Laan
 */
public abstract class GeometryJdbcConverter {
    //definieer placeholder als ? wanneer object naar native geometry wordt 
    //geconverteerd
    //defineer placeholder via native wkt-import functie als geometry als 
    //wkt-string wordt doorgegeven
    public abstract Object convertToNativeGeometryObject(String param) throws SQLException, ParseException;
    public abstract String createPSGeometryPlaceholder() throws SQLException;
    
    public abstract String getSchema();
    public abstract String getGeomTypeName();
    public abstract boolean isDuplicateKeyViolationMessage(String message);
    public abstract String buildPaginationSql(String sql, int offset, int limit);
    public abstract StringBuilder buildLimitSql(StringBuilder sql, int limit);
    public abstract boolean useSavepoints();
    public abstract boolean isPmdKnownBroken();

    public abstract String getMViewsSQL();

    public abstract String getMViewRefreshSQL(String mview);

    public abstract String getGeotoolsDBTypeName();
    
    static public Object convertToSQLObject(String stringValue, ColumnMetadata cm, 
            String tableName, String column) {
        Object param = null;
        stringValue = stringValue.trim();
        switch (cm.getDataType()) {
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.INTEGER:
                try {
                    param = new BigDecimal(stringValue);
                } catch (NumberFormatException nfe) {
                    throw new NumberFormatException(
                            String.format("Conversie van waarde \"%s\" naar type %s voor %s.%s niet mogelijk",
                                    stringValue,
                                    cm.getTypeName(),
                                    tableName,
                                    cm.getName()));
                }
                break;
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
                param = stringValue;
                break;
            case java.sql.Types.DATE:
            case java.sql.Types.TIMESTAMP:
                param = javax.xml.bind.DatatypeConverter.parseDateTime(stringValue);
                if (param != null) {
                    Calendar cal = (Calendar) param;
                    param = new java.sql.Date(cal.getTimeInMillis());
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Data type %s (#%d) van kolom \"%s\" wordt niet ondersteund.", cm.getTypeName(), cm.getDataType(), column));
        }
        return param;
    }

}
