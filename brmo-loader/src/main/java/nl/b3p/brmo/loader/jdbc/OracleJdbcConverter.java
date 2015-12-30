
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
public class OracleJdbcConverter extends GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(OracleJdbcConverter.class);

    private GeometryConverter gc;
    private String schema;

    public OracleJdbcConverter(OracleConnection oc) throws SQLException {

        // geometryFactory argument only required for asGeometry()
        gc = new GeometryConverter(oc, null);
    }

//    @Override
//    public boolean convertsGeometryInsteadOfWkt() {
//        return true;
//    }
//
//    @Override
//    public Object convertGeometry(Geometry geom) throws SQLException {
//        return gc.toSDO(geom, 28992);
//    }
//
//    @Override
//    public Object convertWkt(String wkt) throws SQLException {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ORA-00001:");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        return "SDO_GEOMETRY(?, 28992)";
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

}
