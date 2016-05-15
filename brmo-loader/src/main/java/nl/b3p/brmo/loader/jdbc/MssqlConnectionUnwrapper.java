/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sourceforge.jtds.jdbc.JtdsConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class MssqlConnectionUnwrapper {
    private static final Log LOG = LogFactory.getLog(OracleConnectionUnwrapper.class);

    public static JtdsConnection unwrap(Connection c) throws SQLException {
        LOG.debug("Unwrapping jTDS connection, isWrapperFor(JtdsConnection.class): " + c.isWrapperFor(JtdsConnection.class));
        LOG.debug("Connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection mdC = c.getMetaData().getConnection();
        LOG.debug("MetaData connection class: " + mdC.getClass().getName());

        JtdsConnection oc;

        if (c.isWrapperFor(JtdsConnection.class)) {
            oc = c.unwrap(JtdsConnection.class);
//            String cat = oc.getCatalog();
//            DatabaseMetaData meta = oc.getMetaData();
//            ResultSet cats = meta.getCatalogs();
        } else if (mdC instanceof JtdsConnection) {
            oc = (JtdsConnection) mdC;
        } else {
            throw new SQLException("Kan connectie niet unwrappen naar JtdsConnection!");
        }

        return oc;
    }
}
