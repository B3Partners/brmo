package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.SQLException;

public class GeometryHandlingPreparedStatementBatch extends PreparedStatementQueryBatch {

    private final SQLDialect dialect;
    private final Boolean[] geometryParameterIndexes;
    private final boolean linearizeCurves;

    public GeometryHandlingPreparedStatementBatch(Connection c, String sql, int batchSize,  SQLDialect dialect, Boolean[] geometryParameterIndexes, boolean linearizeCurves) throws SQLException {
        super(c, sql,batchSize);
        this.dialect = dialect;
        this.geometryParameterIndexes = geometryParameterIndexes;
        this.linearizeCurves = linearizeCurves;
    }

    @Override
    protected void setPreparedStatementParameter(int oneBasedParameterIndex, int parameterMetadataType, Object parameter) throws SQLException {
        if (this.geometryParameterIndexes[oneBasedParameterIndex]) {
            this.dialect.setGeometryParameter(c, ps, oneBasedParameterIndex, parameterMetadataType, (Geometry)parameter, linearizeCurves);
        } else {
            super.setPreparedStatementParameter(oneBasedParameterIndex, parameterMetadataType, parameter);
        }
    }
}
