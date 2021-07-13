package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.util.StandardLinearizedWKTWriter;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Supplier;

public class InsertBatch {
    private final Connection c;
    private final SQLDialect dialect;
    private final String insertSql;
    private final boolean pgCopyEnabled;
    private CopyIn copyIn = null;
    private final int batchSize;
    private final StandardLinearizedWKTWriter wktWriter = new StandardLinearizedWKTWriter();
    private final WKTWriter2 wktWriter2 = new WKTWriter2();

    private final Boolean[] geometryParameterIndexes;
    private final boolean linearizeCurves;
    private int count = 0;

    private PreparedStatement ps;
    private int[] parameterTypes;

    public InsertBatch(Connection c, String insertSql, SQLDialect dialect, int batchSize, Boolean[] geometryParameterIndexes, boolean linearizeCurves) throws SQLException {
        this.c = c;
        this.insertSql = insertSql;
        this.pgCopyEnabled = insertSql.startsWith("copy");
        this.dialect = dialect;
        this.batchSize = batchSize;
        this.geometryParameterIndexes = geometryParameterIndexes;
        this.linearizeCurves = linearizeCurves;

        initializePreparedStatement();
    }

    private void initializePreparedStatement() throws SQLException {
        if (pgCopyEnabled) {
            return;
        }
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
        if (pgCopyEnabled) {
            return addCopyBatch(params);
        }

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
            if (pgCopyEnabled) {
                executeCopy();
            } else {
                ps.executeBatch();
            }
            this.count = 0;
        }
    }

    public void close() throws SQLException {
        if (pgCopyEnabled) {
            c.close();
        } else {
            ps.close();
        }
    }

    private boolean addCopyBatch(Object[] params) throws Exception {
        if (copyIn == null) {
            copyIn = (c.unwrap(PGConnection.class)).getCopyAPI().copyIn(insertSql);
        }

        boolean first = true;
        StringBuilder s = new StringBuilder(16384);
        for (Object o: params) {
            if (first) {
                first = false;
            } else {
                s.append("\t");
            }
            if (o == null) {
                s.append("\\N");
            } else {
                // FIXME: currently no escaping of \, \t, \r, \n
                if (o instanceof Geometry) {
                    Geometry geometry = (Geometry)o;
                    String wkt = linearizeCurves ? wktWriter.write(geometry) : wktWriter2.write(geometry);
                    s.append("SRID=").append(geometry.getSRID()).append(";").append(wkt);
                } else {
                    s.append(o);
                }
            }
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

    private void executeCopy() throws SQLException, IOException {
        copyIn.endCopy();
        copyIn = null;
    }
}
