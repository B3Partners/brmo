
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
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ORA-00001:");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        // return "SDO_GEOMETRY(?, 28992)";
        return "?";
    }
    
    @Override
    public Object convertToNativeGeometryObject(String param) throws SQLException, ParseException {
        // return param;
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = param == null || param.trim().length() == 0 ? null : reader.read(param);
        return gc.toSDO(geom, 28992);
    }


    /**
     * @return the schema
     */
    @Override
    public String getSchema() {
        return schema;
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
    
    
    /*
     * Check http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
     * why just WHERE ROWNUM > x AND ROWNUM < x does not work.
     * Using subquery with optimizer.
    */
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from (");
        builder.append("select /*+ FIRST_ROWS(n) */");
        builder.append(" a.*, ROWNUM rnum from (");

        builder.append(sql);

        builder.append(" ) a where ROWNUM <=");

        builder.append(offset + limit);

        builder.append(" )");

        builder.append(" where rnum  > ");
        builder.append(offset);

        return builder.toString();
    }

    @Override
    public boolean useSavepoints() {
        return false;
    }

    @Override
    public boolean isPmdKnownBroken() {
        return false;
    }

}
