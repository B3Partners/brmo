package nl.b3p.brmo.test.util.database;

import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class SequenceUtil {
    private static final Log LOG = LogFactory.getLog(SequenceUtil.class);

    private SequenceUtil() {
    }

    public static void updateSequence(final String sequenceName, final long nextVal, final BasicDataSource ds) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            LOG.debug("update sequence: " + sequenceName);
            // "update" gebruiken omdat we bij oracle stored procedure benaderen?? hoeft niet meer sinds 18c
            // Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).update(conn, geomToJdbc.getUpdateSequenceSQL(sequenceName, nextVal));
            int rowsUpdated = new QueryRunner(geomToJdbc.isPmdKnownBroken()).execute(conn, geomToJdbc.getUpdateSequenceSQL(sequenceName, nextVal));
            LOG.trace("updated sequence: " + sequenceName + ", " + rowsUpdated + " rows updated met SQL: " + geomToJdbc.getUpdateSequenceSQL(sequenceName, nextVal));
        }
    }
}
