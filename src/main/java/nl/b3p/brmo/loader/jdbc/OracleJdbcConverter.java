
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 *
 * @author Matthijs Laan
 */
public class OracleJdbcConverter extends GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(OracleJdbcConverter.class);
    
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    private GeometryConverter gc;
    private String schema;

    public OracleJdbcConverter(OracleConnection oc) throws SQLException {

        // geometryFactory argument only required for asGeometry()
        gc = new GeometryConverter(oc, null);
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        // return "SDO_GEOMETRY(?, 28992)";
        return "?";
    }
    
    @Override
    public Object convertToNativeGeometryObject(Geometry g) throws SQLException, ParseException {
        if(g == null){
            return null;
        }
        String param = g.toText();
        // return param;
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = param == null || param.trim().length() == 0 ? null : reader.read(param);
        return gc.toSDO(geom, 28992);
    }


    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getGeomTypeName() {
        return "SDO_GEOMETRY";
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean isPmdKnownBroken() {
        return true;
    }
}
