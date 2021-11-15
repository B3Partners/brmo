/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.sql;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.StatementConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Wrapper for QueryRunner that logs the SQL to commons-logging. Enable TRACE logging to also log parameters.
 */
public class LoggingQueryRunner extends QueryRunner {
    private static final Log LOG = LogFactory.getLog(LoggingQueryRunner.class);

    public LoggingQueryRunner() {
        super();
    }

    public LoggingQueryRunner(boolean pmdKnownBroken) {
        super(pmdKnownBroken);
    }
    public LoggingQueryRunner(DataSource ds) {
        super(ds);
    }
    public LoggingQueryRunner(StatementConfiguration stmtConfig) {
        super(stmtConfig);
    }
    public LoggingQueryRunner(DataSource ds, boolean pmdKnownBroken) {
        super(ds, pmdKnownBroken);
    }
    public LoggingQueryRunner(DataSource ds, StatementConfiguration stmtConfig) {
        super(ds, stmtConfig);
    }
    public LoggingQueryRunner(DataSource ds, boolean pmdKnownBroken, StatementConfiguration stmtConfig) {
        super(ds, pmdKnownBroken, stmtConfig);
    }

    private void log(String type, String sql, Object params) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(type + ": " + sql + (params != null ? ", params: " + params : ""));
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(type + ": " + sql);
        }
    }

    @Override
    public int[] batch(Connection conn, String sql, Object[][] params) throws SQLException {
        log("batch", sql, params);
        return super.batch(conn, sql, params);
    }

    @Override
    public int[] batch(String sql, Object[][] params) throws SQLException {
        log("batch", sql, params);
        return super.batch(sql, params);
    }

    @Override
    @Deprecated
    public <T> T query(Connection conn, String sql, Object param, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, param);
        return super.query(conn, sql, param, rsh);
    }

    @Override
    @Deprecated
    public <T> T query(Connection conn, String sql, Object[] params, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, params);
        return super.query(conn, sql, params, rsh);
    }

    @Override
    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("query", sql, params);
        return super.query(conn, sql, rsh, params);
    }

    @Override
    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, null);
        return super.query(conn, sql, rsh);
    }

    @Override
    @Deprecated
    public <T> T query(String sql, Object param, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, param);
        return super.query(sql, param, rsh);
    }

    @Override
    @Deprecated
    public <T> T query(String sql, Object[] params, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, params);
        return super.query(sql, params, rsh);
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("query", sql, params);
        return super.query(sql, rsh, params);
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
        log("query", sql, null);
        return super.query(sql, rsh);
    }

    @Override
    public int update(Connection conn, String sql) throws SQLException {
        log("update", sql, null);
        return super.update(conn, sql);
    }


    @Override
    public int update(Connection conn, String sql, Object param) throws SQLException {
        log("update", sql, param);
        return super.update(conn, sql, param);
    }

    @Override
    public int update(Connection conn, String sql, Object... params) throws SQLException {
        log("update", sql, params);
        return super.update(conn, sql, params);
    }

    @Override
    public int update(String sql) throws SQLException {
        log("update", sql, null);
        return super.update(sql);
    }

    @Override
    public int update(String sql, Object param) throws SQLException {
        log("update", sql, param);
        return super.update(sql, param);
    }

    @Override
    public int update(String sql, Object... params) throws SQLException {
        log("update", sql, params);
        return super.update(sql, params);
    }

    @Override
    public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
        log("insert", sql, null);
        return super.insert(sql, rsh);
    }

    @Override
    public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("insert", sql, params);
        return super.insert(sql, rsh, params);
    }

    @Override
    public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
        log("insert", sql, null);
        return super.insert(conn, sql, rsh);
    }

    @Override
    public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("insert", sql, params);
        return super.insert(conn, sql, rsh, params);
    }

    @Override
    public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) throws SQLException {
        log("insertBatch", sql, params);
        return super.insertBatch(sql, rsh, params);
    }

    @Override
    public <T> T insertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, Object[][] params) throws SQLException {
        log("insertBatch", sql, params);
        return super.insertBatch(conn, sql, rsh, params);
    }

    @Override
    public int execute(Connection conn, String sql, Object... params) throws SQLException {
        log("execute", sql, params);
        return super.execute(conn, sql, params);
    }

    @Override
    public int execute(String sql, Object... params) throws SQLException {
        log("execute", sql, params);
        return super.execute(sql, params);
    }

    @Override
    public <T> List<T> execute(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("execute", sql, params);
        return super.execute(conn, sql, rsh, params);
    }

    @Override
    public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        log("execute", sql, params);
        return super.execute(sql, rsh, params);
    }
}
