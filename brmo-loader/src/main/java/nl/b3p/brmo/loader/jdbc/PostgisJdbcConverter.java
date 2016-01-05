
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.io.ParseException;
import java.sql.SQLException;
import org.postgis.PGgeometry;

/**
 *
 * @author Matthijs Laan
 */
public class PostgisJdbcConverter extends GeometryJdbcConverter {

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
        return new PGgeometry("SRID=28992;" + param);
    }

    @Override
    public String getSchema() {
        return null;
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
    public boolean useSavepoints() {
        return true;
    }

     @Override
    public boolean isPmdKnownBroken() {
        return false;
    }

}
