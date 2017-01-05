/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import net.sourceforge.jtds.jdbc.JtdsConnection;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class MssqlConnectionUnwrapper {

    private static final Log LOG = LogFactory.getLog(MssqlConnectionUnwrapper.class);

    /**
     * <strong>LET OP</strong> JtdsConnection#isWrapperFor werpt altijd een
     * AbstractMethodError want niet geimplementeerd!
     *
     * @param c de wrapped JtdsConnection
     * @return de unwrapped JtdsConnection
     * @throws SQLException Als unwrappen naar een JtdsConnection is mislukt
     */
    public static JtdsConnection unwrap(Connection c) throws SQLException {
        LOG.debug("Unwrapping jTDS connection, connection class: " + c.getClass().getName());
        // Sometimes isWrapperFor() does not work for certain JDBC drivers. The
        // MetaData connection is always unwrapped, trick learned from Spring's
        // org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor
        Connection mdC = c.getMetaData().getConnection();
        LOG.debug("MetaData connection class: " + mdC.getClass().getName());

        JtdsConnection oc = null;

        try {
            if (mdC.isWrapperFor(JtdsConnection.class)) {
                oc = mdC.unwrap(JtdsConnection.class);
            }
        } catch (AbstractMethodError e) {
            LOG.debug("Connection ondersteund geen 'isWrapperFor' of 'unwrap' - dit is verwacht voor een DelegatingConnection|PoolableConnection.");
        }
        if (oc == null) {
            if (mdC instanceof JtdsConnection) {
                oc = (JtdsConnection) mdC;
            } else if (mdC instanceof PoolableConnection) {
                oc = (JtdsConnection) ((DelegatingConnection) mdC).getDelegate();
            } else {
                throw new SQLException("Kan connectie niet unwrappen naar JtdsConnection!");
            }
        }
        return oc;
    }
}
