package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.text.StringEscapeUtils;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

/**
 * The PostgreSQL JDBC driver does not support parallel copy operations, even with multiple connections. So this class
 * can cache the copy stream in memory so only one copy operation is active at a time, although this is significantly
 * slower.
 */
public class PostGISCopyInsertBatch implements QueryBatch {
    protected Connection connection;
    protected String sql;
    protected PostGISDialect dialect;
    protected int batchSize;
    protected boolean linearizeCurves;

    protected int count = 0;
    protected StringEscapeUtils.Builder copyData = PostgresCopyEscapeUtils.builder();
    protected CopyIn copyIn = null;
    protected CopyIn lastCopyIn = null;
    protected boolean buffer;

    public PostGISCopyInsertBatch(Connection connection, String sql, int batchSize, SQLDialect dialect, boolean buffer, boolean linearizeCurves) {
        if (!(dialect instanceof PostGISDialect)) {
            throw new IllegalArgumentException();
        }
        this.connection = connection;
        this.sql = sql;
        this.dialect = (PostGISDialect)dialect;
        this.batchSize = batchSize;
        this.buffer = buffer;
        this.linearizeCurves = linearizeCurves;
    }

    protected void createCopyIn() throws SQLException {
        PGConnection pgConnection = connection.unwrap(PGConnection.class);
        this.copyIn = pgConnection.getCopyAPI().copyIn(sql);
    }

    public static Duration copyDuration = Duration.ofSeconds(0);

    protected void writeToCopy() throws SQLException {
        if (copyIn == null) {
            createCopyIn();
        }
        Instant start = Instant.now();
        byte[] bytes = copyData.toString().getBytes(StandardCharsets.UTF_8);
        copyIn.writeToCopy(bytes, 0, bytes.length);
        copyDuration = copyDuration.plus(Duration.between(start, Instant.now()));
        copyData = PostgresCopyEscapeUtils.builder();
    }

    @Override
    public boolean addBatch(Object[] params) throws Exception {
        for(int i = 0; i < params.length; i++) {
            if (i != 0) {
                copyData.append("\t");
            }
            Object param = params[i];
            if (param == null) {
                copyData.append("\\N");
            } else if (param instanceof Geometry) {
                Geometry geometry = (Geometry) param;
                copyData.append(dialect.getEWkt(geometry, linearizeCurves));
            } else if (param instanceof Boolean) {
                copyData.append((Boolean)param ? "t" : "f");
            } else {
                // TODO any more types need special conversion?
                copyData.escape(param.toString());
            }
        }
        copyData.append("\n");

        if (!buffer) {
            writeToCopy();
        }

        count++;
        if (count == batchSize) {
            this.executeBatch();
            return true;
        }
        return false;
    }


    @Override
    public void executeBatch() throws Exception {
        if (count > 0) {
            if (buffer) {
                writeToCopy();
            }
            Instant start = Instant.now();
            copyIn.endCopy();
            copyDuration = copyDuration.plus(Duration.between(start, Instant.now()));
            lastCopyIn = copyIn;
            // reset so writeToCopy will create a new one
            copyIn = null;
            count = 0;
        }
    }

    @Override
    public void close() {
    }
}
