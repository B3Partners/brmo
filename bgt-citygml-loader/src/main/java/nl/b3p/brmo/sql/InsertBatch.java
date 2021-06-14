package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class InsertBatch {
    private final Connection c;
    private final SQLDialect dialect;
    private final String insertSql;
    private final int batchSize;

    private final Boolean[] geometryParameterIndexes;
    private final boolean linearizeCurves;
    private int count = 0;

    private PreparedStatement ps;
    private int[] parameterTypes;

    public InsertBatch(Connection c, String insertSql, SQLDialect dialect, int batchSize, Boolean[] geometryParameterIndexes, boolean linearizeCurves) throws SQLException {
        this.c = c;
        this.insertSql = insertSql;
        this.dialect = dialect;
        this.batchSize = batchSize;
        this.geometryParameterIndexes = geometryParameterIndexes;
        this.linearizeCurves = linearizeCurves;

        initializePreparedStatement();
    }

    private void initializePreparedStatement() throws SQLException {
        ps = c.prepareStatement(insertSql);
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
    public boolean addBatch(Object[] params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            int parameterIndex = i + 1;
            try {
                int pmdType = parameterTypes != null ? parameterTypes[i] : Types.VARCHAR;
                if (geometryParameterIndexes[i]) {
                    dialect.setGeometryParameter(ps, parameterIndex, pmdType, (Geometry) params[i], linearizeCurves);
                } else {
                    if (params[i] != null) {
                        ps.setObject(parameterIndex, params[i]);
                    } else {
                        ps.setNull(parameterIndex, pmdType);
                    }
                }
            } catch (Exception e) {
                throw new Exception(String.format("Exception setting parameter %s to value %s for insert SQL \"%s\"", i, params[i], insertSql), e);
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

    public void executeBatch() throws Exception {
        if (count > 0) {
            ps.executeBatch();
            this.count = 0;
        }
    }

    public void close() throws SQLException {
        ps.close();
    }
}
