
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.io.ParseException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.db.sqlserver.Encoders;


/**
 *
 * @author Matthijs Laan
 */
public class MssqlJdbcConverter extends GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(MssqlJdbcConverter.class);

    private String schema = "dbo";

   
    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        //return "geometry::STGeomFromText(?, 28992)";
        return "?";
    }

    @Override
    public Object convertToNativeGeometryObject(com.vividsolutions.jts.geom.Geometry g) throws SQLException, ParseException {
        if(g == null){
            return null;
        }
        String param = g.toText();
        // return param;
        if (param == null || param.trim().length() == 0) {
            return null;
        }
        Geometry geom = Wkt.fromWkt("SRID=28992; " + param);
        byte[] ret = Encoders.encode(geom);
        return ret;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "geometry";
    }

    @Override
    public com.vividsolutions.jts.geom.Geometry convertToJTSGeometryObject(Object nativeObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
    @Override
    public boolean isPmdKnownBroken() {
        //return true; // microsoft driver
        return false; // jtds driver
    }
}
