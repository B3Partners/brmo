/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PreparedStatementQueryBatch implements QueryBatch {
    protected final Connection c;
    private final String sql;
    private final int batchSize;

    private int count = 0;

    protected PreparedStatement ps;
    private int[] parameterTypes;

    public PreparedStatementQueryBatch(Connection c, String sql, int batchSize) throws SQLException {
        this.c = c;
        this.sql = sql;
        this.batchSize = batchSize;
        initializePreparedStatement();
    }

    private void initializePreparedStatement() throws SQLException {
        ps = c.prepareStatement(sql);
        parameterTypes = null;
        try {
            ParameterMetaData pmd = ps.getParameterMetaData();
            if (pmd != null) {
                parameterTypes = new int[pmd.getParameterCount()];
                for (int i = 0; i < pmd.getParameterCount(); i++) {
                    parameterTypes[i] = pmd.getParameterType(i + 1);
                }
            }
        } catch(Exception e) {
            // May fail, for example if Oracle table is in NOLOGGING mode. Ignore.
        }
    }

    /**
     * Add a batch and execute if batch size reached.
     * @param params The parameters for the row to insert
     * @return Whether the batch was executed
     */
    @Override
    public boolean addBatch(Object[] params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            int parameterIndex = i + 1;
            try {
                int pmdType = parameterTypes != null ? parameterTypes[i] : Types.VARCHAR;
                setPreparedStatementParameter(parameterIndex, pmdType, params[i]);
            } catch (Exception e) {
                throw new Exception(String.format("Exception setting parameter %s to value %s for insert SQL \"%s\"", i, params[i], sql), e);
            }
        }
        ps.addBatch();

        count++;

        if (count == batchSize) {
            this.executeBatch();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void executeBatch() throws Exception {
        if (count > 0) {
            //System.out.printf("Executing %d batches for sql \"%s\"\n", count, sql);
            ps.executeBatch();
            this.count = 0;
        }
    }

    @Override
    public void close() throws SQLException {
        ps.close();
    }

    protected void setPreparedStatementParameter(int oneBasedParameterIndex, int parameterMetadataType, Object parameter) throws SQLException {
        if (parameter != null) {
            ps.setObject(oneBasedParameterIndex, parameter);
        } else {
            ps.setNull(oneBasedParameterIndex, parameterMetadataType);
        }
    }
}
