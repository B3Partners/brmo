
package nl.b3p.brmo.loader.jdbc;

import java.sql.SQLException;

/**
 *
 * @author Matthijs Laan
 */
public class PostgisJdbcConverter extends GeometryJdbcConverter {

//    @Override
//    public boolean convertsGeometryInsteadOfWkt() {
//        return false;
//    }
//
//    @Override
//    public Object convertGeometry(Geometry geom) throws SQLException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Object convertWkt(String wkt) throws SQLException {
//        return new PGgeometry("SRID=28992;" + wkt);
//    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        return message!=null && message.startsWith("ERROR: duplicate key value violates unique constraint");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        return "ST_GeomFromText(?, 28992)";
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

}
