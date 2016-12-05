
package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class OracleConnectionUnwrapper {
    private static final Log log = LogFactory.getLog(OracleConnectionUnwrapper.class);

    public static OracleConnection unwrap(Connection c) throws SQLException {
        log.debug("Unwrapping Oracle connection, isWrapperFor(OracleConnection.class): " + c.isWrapperFor(OracleConnection.class));
        log.debug("Connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection mdC = c.getMetaData().getConnection();
        log.debug("MetaData connection class: " + mdC.getClass().getName());

        OracleConnection oc;

        if(c.isWrapperFor(OracleConnection.class)) {
            oc = c.unwrap(OracleConnection.class);
        } else if(mdC instanceof OracleConnection) {
            oc = (OracleConnection)mdC;
        } else {
            throw new SQLException("Kan connectie niet unwrappen naar OracleConnection!");
        }

        return oc;
    }
}
