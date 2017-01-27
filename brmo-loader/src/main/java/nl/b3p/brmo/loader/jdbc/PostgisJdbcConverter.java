
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.io.ParseException;
import java.sql.SQLException;
import org.postgis.PGgeometry;

/**
 *
 * @author Matthijs Laan
 */
public class PostgisJdbcConverter extends GeometryJdbcConverter {

    private String schema = null;

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ERROR: duplicate key value violates unique constraint");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        //return "ST_GeomFromText(?, 28992)";
        return "?";
    }
   
    @Override
    public Object convertToNativeGeometryObject(String param) throws SQLException, ParseException {
        //return param;
        if (param == null || param.trim().length() == 0) {
            return null;
        }
        return new PGgeometry("SRID=28992;" + param);
    }

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "geometry";
    }
    
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" LIMIT ");
        builder.append(limit);
        builder.append(" OFFSET ");
        builder.append(offset);
        return builder.toString();
    }

    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        String s = buildPaginationSql(sql.toString(), 0, limit);
        return new StringBuilder(s);
    }

    @Override
    public boolean useSavepoints() {
        return true;
    }

     @Override
    public boolean isPmdKnownBroken() {
        return false;
    }
    @Override
    public String getGeotoolsDBTypeName() {
        return "postgis";
    }

    @Override
    public String getMViewsSQL() {
        return "SELECT oid::regclass::text FROM pg_class WHERE relkind = 'm'";
    }

    @Override
    public String getMViewRefreshSQL(String mview) {
        return String.format("REFRESH MATERIALIZED VIEW %s;", mview);
    }
}
