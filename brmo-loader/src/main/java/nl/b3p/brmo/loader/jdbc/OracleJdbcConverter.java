
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.oracle.sdo.GeometryConverter;

/**
 *
 * @author Matthijs Laan
 */
public class OracleJdbcConverter implements GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(OracleJdbcConverter.class);

    private GeometryConverter gc;

    public OracleJdbcConverter(OracleConnection oc) throws SQLException {

        // geometryFactory argument only required for asGeometry()
        gc = new GeometryConverter(oc, null);
    }

    @Override
    public boolean convertsGeometryInsteadOfWkt() {
        return true;
    }

    @Override
    public Object convertGeometry(Geometry geom) throws SQLException {
        return gc.toSDO(geom, 28992);
    }

    @Override
    public Object convertWkt(String wkt) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public static boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ORA-00001:");
    }
}
