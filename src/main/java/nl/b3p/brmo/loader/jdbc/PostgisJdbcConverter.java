
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public Object convertToNativeGeometryObject(Geometry g) throws SQLException, ParseException {
        if(g == null){
            return null;
        }
        String param = g.toText();
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
        PGgeometry geom = (PGgeometry)nativeObj;
        StringBuffer sb = new StringBuffer();
        geom.getGeometry().outerWKT(sb);
        try {
            return wkt.read(sb.toString());
        } catch (ParseException ex) {
            return null;
        }
    }
    
    @Override
    public boolean isPmdKnownBroken() {
        return false;
    }
}
