package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;

public class InsertBatch {
    private final Connection c;
    private final SQLDialect dialect;
    private final String insertSql;
    private final Object[][] batch;

    private final Boolean[] geometryParameterIndexes;
    private final boolean linearizeCurves;
    private int index = 0;

    public InsertBatch(Connection c, String insertSql, SQLDialect dialect, int batchSize, Boolean[] geometryParameterIndexes, boolean linearizeCurves) {
        this.c = c;
        this.insertSql = insertSql;
        this.dialect = dialect;
        this.batch = new Object[batchSize][];
        this.geometryParameterIndexes = geometryParameterIndexes;
        this.linearizeCurves = linearizeCurves;
    }

    public boolean addBatch(Object[] params) throws Exception {
        // TODO do not buffer here, but immediately call to PreparedStatement.addBatch()
        this.batch[this.index++] = params;

        if (index == batch.length) {
            this.executeBatch();
            return true;
        } else {
            return false;
        }
    }

    public void executeBatch() throws Exception {
        if (index > 0) {
            Object[][] thisBatch = batch;
            if (index < batch.length) {
                thisBatch = Arrays.copyOfRange(batch, 0, index);
            }

            PreparedStatement ps = c.prepareStatement(insertSql);
            int[] parameterTypes = null;
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
            try {
                for(Object[] params: thisBatch) {
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
                        } catch(Exception e) {
                            throw new Exception(String.format("Exception setting parameter %s to value %s for insert SQL \"%s\"", i, params[i], insertSql), e);
                        }
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            } finally {
                ps.close();
            }

            this.index = 0;
        }
    }
}
