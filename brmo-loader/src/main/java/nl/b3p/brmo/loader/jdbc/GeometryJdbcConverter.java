
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.SQLException;

/**
 *
 * @author Matthijs Laan
 */
public interface GeometryJdbcConverter {
    public boolean convertsGeometryInsteadOfWkt();

    public Object convertGeometry(Geometry geom) throws SQLException;
    public Object convertWkt(String wkt) throws SQLException;
//    public String convertToWkt(Object geom) throws SQLException;
}
