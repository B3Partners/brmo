
package nl.b3p.brmo.loader.jdbc;

import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class MssqlJdbcConverter extends GeometryJdbcConverter {

    private static final Log log = LogFactory.getLog(MssqlJdbcConverter.class);

//    @Override
//    public boolean convertsGeometryInsteadOfWkt() {
//        return true;
//    }
//
//    @Override
//    public Object convertGeometry(Geometry geom) throws SQLException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Object convertWkt(String wkt) throws SQLException {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public boolean isDuplicateKeyViolationMessage(String message) {
        //Error Code: 2627
        //Violation of %ls constraint '%.*ls'. Cannot insert duplicate key in object '%.*ls'.
        return message!=null && message.contains("Cannot insert duplicate key in object");
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        return "geometry::STGeomFromText(?, 28992)";
    }

    @Override
    public String getSchema() {
        return "dbo";
    }

    @Override
    public String getGeomTypeName() {
        return "geometry";
    }
    
    /*
    QUERY USING "ROW_NUMBER"
    DECLARE @PageNumber AS INT, @RowspPage AS INT
    SET @PageNumber = 2
    SET @RowspPage = 10 
    SELECT * FROM (
                 SELECT ROW_NUMBER() OVER(ORDER BY ID_EXAMPLE) AS Numero,
                        ID_EXAMPLE, NM_EXAMPLE , DT_CREATE FROM TB_EXAMPLE
                   ) AS TBL
    WHERE Numero BETWEEN ((@PageNumber - 1) * @RowspPage + 1) AND (@PageNumber * @RowspPage)
    ORDER BY ID_EXAMPLE
    GO

    QUERY USING "OFFSET" AND "FETCH NEXT" (SQL SERVER 2012)
    DECLARE @PageNumber AS INT, @RowspPage AS INT
    SET @PageNumber = 2
    SET @RowspPage = 10
    
    SELECT ID_EXAMPLE, NM_EXAMPLE, DT_CREATE
    FROM TB_EXAMPLE
    ORDER BY ID_EXAMPLE
    OFFSET ((@PageNumber - 1) * @RowspPage) ROWS
    FETCH NEXT @RowspPage ROWS ONLY
    GO    
    */
    @Override
    public String buildPaginationSql(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" OFFSET ");
        builder.append(offset);
        builder.append(" ROWS FETCH NEXT ");
        builder.append(limit);
        builder.append(" ROWS ONLY ");
        return builder.toString();
    }

    @Override
    public boolean useSavepoints() {
        return false;
    }

}
