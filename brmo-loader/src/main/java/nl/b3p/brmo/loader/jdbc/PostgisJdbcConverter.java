
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.SQLException;
import org.postgis.PGgeometry;

/**
 *
 * @author Matthijs Laan
 */
public class PostgisJdbcConverter implements GeometryJdbcConverter {

    @Override
    public boolean convertsGeometryInsteadOfWkt() {
        return false;
    }

    public Object convertGeometry(Geometry geom) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object convertWkt(String wkt) throws SQLException {
        return new PGgeometry("SRID=28992;" + wkt);
    }

    public static boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ERROR: duplicate key value violates unique constraint");
    }
}
