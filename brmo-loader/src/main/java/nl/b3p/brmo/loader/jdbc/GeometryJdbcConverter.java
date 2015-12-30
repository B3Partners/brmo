
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author Matthijs Laan
 */
public abstract class GeometryJdbcConverter {
//    public boolean convertsGeometryInsteadOfWkt();
//
//    public Object convertGeometry(Geometry geom) throws SQLException;
//    public Object convertWkt(String wkt) throws SQLException;
    public abstract String createPSGeometryPlaceholder() throws SQLException;
    public abstract String getSchema();
    public abstract String getGeomTypeName();
    public abstract boolean isDuplicateKeyViolationMessage(String message);
    public abstract String buildPaginationSql(String sql, int offset, int limit);
    public abstract boolean useSavepoints();
    
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
