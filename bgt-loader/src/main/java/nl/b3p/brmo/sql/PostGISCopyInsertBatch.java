/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The PostgreSQL JDBC driver does not support parallel copy operations, even with multiple connections. So this class
 * can cache the copy stream in memory so only one copy operation is active at a time, although this is significantly
 * slower.
 */
public class PostGISCopyInsertBatch implements QueryBatch {
    private static final Log LOG = LogFactory.getLog(PostGISCopyInsertBatch.class);

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

    protected void writeToCopy() throws SQLException {
        if (copyIn == null) {
            createCopyIn();
        }
        byte[] bytes = copyData.toString().getBytes(StandardCharsets.UTF_8);
        copyIn.writeToCopy(bytes, 0, bytes.length);
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
                if (LOG.isDebugEnabled()) {
                    // To get the number of bytes this call is duplicated from writeToCopy()...
                    int bytes = copyData.toString().getBytes(StandardCharsets.UTF_8).length;
                    LOG.debug(String.format("execute buffered copy batch, %d bytes, %d rows, sql: %s", bytes, count, sql));
                }
                writeToCopy();
            } else {
                LOG.debug(String.format("execute copy batch, %d rows, sql: %s", count, sql));
            }
            copyIn.endCopy();
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
