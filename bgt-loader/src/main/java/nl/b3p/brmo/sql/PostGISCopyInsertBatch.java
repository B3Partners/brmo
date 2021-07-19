package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public class PostGISCopyInsertBatch implements QueryBatch {
    protected Connection connection;
    protected String sql;
    protected PostGISDialect dialect;
    protected int batchSize;
    protected boolean linearizeCurves;

    protected int count = 0;
    protected CopyIn copyIn = null;

    public PostGISCopyInsertBatch(Connection connection, String sql, int batchSize, SQLDialect dialect, boolean linearizeCurves) {
        if (!(dialect instanceof PostGISDialect)) {
            throw new IllegalArgumentException();
        }
        this.connection = connection;
        this.sql = sql;
        this.dialect = (PostGISDialect)dialect;
        this.batchSize = batchSize;
        this.linearizeCurves = linearizeCurves;
    }

    @Override
    public boolean addBatch(Object[] params) throws Exception {
        if (count == 0) {
            copyIn = (connection.unwrap(PGConnection.class)).getCopyAPI().copyIn(sql);
        }
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < params.length; i++) {
            if (i != 0) {
                s.append("\t");
            }
            Object param = params[i];
            String value;
            if (param == null) {
                value = "\\N";
            } else if (param instanceof Geometry) {
                Geometry geometry = (Geometry) param;
                value = dialect.getEWkt(geometry, linearizeCurves);
            } else if (param instanceof Boolean) {
                value = ((Boolean) param) ? "t" : "f";
            } else {
                // FIXME any more types need special conversion?
                // FIXME currently no escaping of \, \t, \r, \n (so line endings and tabs not supported in values!)
                value = param.toString();
            }
            s.append(value);
        }
        s.append("\n");
        byte[] bytes = s.toString().getBytes(StandardCharsets.UTF_8);
        copyIn.writeToCopy(bytes, 0, bytes.length);

        count++;
        if (count == batchSize) {
            this.executeBatch();
            return true;
        }
        return false;
    }

    @Override
    public void executeBatch() throws Exception {
        copyIn.endCopy();
        count = 0;
    }

    @Override
    public void close() throws SQLException {
        if (copyIn.isActive()) {
            copyIn.cancelCopy();
        }
    }
}
