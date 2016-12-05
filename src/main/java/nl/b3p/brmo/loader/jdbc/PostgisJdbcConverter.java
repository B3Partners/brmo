
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
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

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "geometry";
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
