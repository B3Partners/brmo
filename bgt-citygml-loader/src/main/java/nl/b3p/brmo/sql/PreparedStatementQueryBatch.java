package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PreparedStatementQueryBatch implements QueryBatch {
    private final Connection c;
    private final SQLDialect dialect;
    private final String sql;
    private final int batchSize;

    private final Boolean[] geometryParameterIndexes;
    private final boolean linearizeCurves;
    private int count = 0;

    private PreparedStatement ps;
    private int[] parameterTypes;

    public PreparedStatementQueryBatch(Connection c, String sql, SQLDialect dialect, int batchSize, Boolean[] geometryParameterIndexes, boolean linearizeCurves) throws SQLException {
        this.c = c;
        this.sql = sql;
        this.dialect = dialect;
        this.batchSize = batchSize;
        this.geometryParameterIndexes = geometryParameterIndexes;
        this.linearizeCurves = linearizeCurves;

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
                if (geometryParameterIndexes[i]) {
                    dialect.setGeometryParameter(c, ps, parameterIndex, pmdType, (Geometry) params[i], linearizeCurves);
                } else {
                    if (params[i] != null) {
                        ps.setObject(parameterIndex, params[i]);
                    } else {
                        ps.setNull(parameterIndex, pmdType);
                    }
                }
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
}
