package nl.b3p.brmo.test.util.database;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SequenceUtil {
  private static final Log LOG = LogFactory.getLog(SequenceUtil.class);

  private SequenceUtil() {}

  public static void updateSequence(
      final String sequenceName, final long nextVal, final DataSource ds) throws SQLException {
    try (Connection conn = ds.getConnection()) {
      conn.setAutoCommit(true);
      final GeometryJdbcConverter geomToJdbc =
          GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
      LOG.debug("update sequence: " + sequenceName);
      int rowsUpdated =
          new QueryRunner(geomToJdbc.isPmdKnownBroken())
              .execute(conn, geomToJdbc.getUpdateSequenceSQL(sequenceName, nextVal));
      LOG.trace(
          "updated sequence: "
              + sequenceName
              + ", "
              + rowsUpdated
              + " rows updated met SQL: "
              + geomToJdbc.getUpdateSequenceSQL(sequenceName, nextVal));
    }
  }
}
