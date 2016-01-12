package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;

/**
 *
 * @author Chris
 */
public class GeometryJdbcConverterFactory {

    public static GeometryJdbcConverter getGeometryJdbcConverter(Connection conn) {
        String databaseProductName = null;
        try {
            databaseProductName = conn.getMetaData().getDatabaseProductName();
        } catch (SQLException ex) {
            throw new UnsupportedOperationException("Cannot get database product name", ex);
        }
        if (databaseProductName.contains("PostgreSQL")) {
            return new PostgisJdbcConverter();
        } else if (databaseProductName.contains("Oracle")) {
            try {
                OracleConnection oc = OracleConnectionUnwrapper.unwrap(conn);
                OracleJdbcConverter geomToJdbc = new OracleJdbcConverter(oc);
                geomToJdbc.setSchema(oc.getCurrentSchema());
                return geomToJdbc;
            } catch (SQLException ex) {
                throw new UnsupportedOperationException("Cannot get connection: " + databaseProductName, ex);
            }
        } else if (databaseProductName.contains("Microsoft SQL Server")) {
            return new MssqlJdbcConverter();
        } else {
            throw new UnsupportedOperationException("Unknown database: " + databaseProductName);
        }
    }
}
