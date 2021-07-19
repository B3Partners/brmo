package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.SQLException;

public class GeometryHandlingPreparedStatementBatch extends PreparedStatementQueryBatch {

    private final SQLDialect dialect;
    private final Boolean[] parameterIsGeometry;
    private final boolean linearizeCurves;

    public GeometryHandlingPreparedStatementBatch(Connection c, String sql, int batchSize, SQLDialect dialect, Boolean[] parameterIsGeometry, boolean linearizeCurves) throws SQLException {
        super(c, sql,batchSize);
        this.dialect = dialect;
        this.parameterIsGeometry = parameterIsGeometry;
        this.linearizeCurves = linearizeCurves;
    }

    @Override
    protected void setPreparedStatementParameter(int oneBasedParameterIndex, int parameterMetadataType, Object parameter) throws SQLException {
        if (this.parameterIsGeometry[oneBasedParameterIndex-1]) {
            this.dialect.setGeometryParameter(c, ps, oneBasedParameterIndex, parameterMetadataType, (Geometry)parameter, linearizeCurves);
        } else {
            super.setPreparedStatementParameter(oneBasedParameterIndex, parameterMetadataType, parameter);
        }
    }
}
