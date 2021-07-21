package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.BGTObjectStreamer;
import nl.b3p.brmo.bgt.loader.BGTObjectTableWriter;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.METADATA_TABLE;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.Metadata;

public class IMGeoDb {

    public enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    private final SQLDialect dialect;
    private final DatabaseOptions dbOptions;
    private Connection connection;

    public IMGeoDb(DatabaseOptions dbOptions) throws SQLException, ClassNotFoundException {
        this.dbOptions = dbOptions;
        String connectionString = dbOptions.connectionString;
        SQLDialectEnum sqlDialectEnum;
        if (connectionString.startsWith("jdbc:postgresql:")) {
            sqlDialectEnum = SQLDialectEnum.postgis;
        } else if (connectionString.startsWith("jdbc:oracle:thin:")) {
            sqlDialectEnum = SQLDialectEnum.oracle;
        } else if (connectionString.startsWith("jdbc:sqlserver:")) {
            sqlDialectEnum = SQLDialectEnum.mssql;
        } else {
            throw new IllegalArgumentException(String.format("Can't determine database dialect from connection string \"%s\"", connectionString));
        }

        dialect = createDialect(sqlDialectEnum);
        dialect.loadDriver();
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = createConnection();
        }
        return this.connection;
    }

    public void closeConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

    public Connection createConnection() {
        try {
            return DriverManager.getConnection(dbOptions.connectionString, dbOptions.user, dbOptions.password);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Error connecting to the database with connection string \"%s\"", dbOptions.connectionString), e);
        }
    }

    public BGTObjectTableWriter createObjectTableWriter(LoadOptions loadOptions, DatabaseOptions dbOptions) throws SQLException, ClassNotFoundException {
        BGTObjectTableWriter writer = new BGTObjectTableWriter(getConnection(), this.getDialect());

        if (loadOptions == null) {
            loadOptions = new LoadOptions();
        }
        writer.setBatchSize(dbOptions.batchSize != null ? dbOptions.batchSize : this.getDialect().getDefaultOptimalBatchSize());
        writer.setMultithreading(loadOptions.multithreading);
        writer.setUsePgCopy(dbOptions.usePgCopy);
        writer.setObjectLimit(loadOptions.maxObjects);
        writer.setLinearizeCurves(loadOptions.linearizeCurves);
        writer.setCurrentObjectsOnly(!loadOptions.includeHistory);
        writer.setCreateSchema(loadOptions.createSchema);
        writer.setTablePrefix(loadOptions.tablePrefix);
        return writer;
    }

    public static SQLDialect createDialect(SQLDialectEnum dialectEnum) throws SQLException {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect();
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException(String.format("Invalid dialect: \"%s\"", dialectEnum));
    }

    public String getMetadata(Metadata key) throws SQLException {
        return new QueryRunner().query(getConnection(), "select value from " + METADATA_TABLE + " where id = ?", new ScalarHandler<>(), key.getDbKey());
    }

    public void setMetadataValue(Metadata key, String value) throws Exception {
        try {
            int updated = new QueryRunner().update(getConnection(), "update " + METADATA_TABLE + " set value = ? where id = ?", value, key.getDbKey());
            if (updated == 0) {
                new QueryRunner().update(getConnection(), "insert into " + METADATA_TABLE + "(id, value) values(?,?)", key.getDbKey(), value);
            }
        } catch (SQLException e) {
            throw new Exception(String.format("Error updating metadata key \"%s\" with value \"%s\": %s", key.getDbKey(), value, e.getMessage()), e);
        }
    }

    public void setMetadataForMutaties(BGTObjectStreamer.MutatieInhoud mutatieInhoud) throws Exception {
        setMetadataValue(Metadata.DELTA_TIME_TO, null);
        if (mutatieInhoud == null || mutatieInhoud.getLeveringsId() == null) {
            setMetadataValue(Metadata.INITIAL_LOAD_DELTA_ID, null);
            setMetadataValue(Metadata.INITIAL_LOAD_TIME, null);
            setMetadataValue(Metadata.DELTA_ID, null);
        } else {
            String deltaId = mutatieInhoud.getLeveringsId();

            if ("initial".equals(mutatieInhoud.getMutatieType())) {
                setMetadataValue(Metadata.INITIAL_LOAD_DELTA_ID, deltaId);
                setMetadataValue(Metadata.INITIAL_LOAD_TIME, Instant.now().toString());
            }
            setMetadataValue(Metadata.DELTA_ID, deltaId);
        }
    }

    public void setFeatureTypesEnumMetadata(Set<DeltaCustomDownloadRequest.FeaturetypesEnum> featureTypes) throws Exception {
        setMetadataValue(Metadata.FEATURE_TYPES,
                        featureTypes.stream()
                        .map(DeltaCustomDownloadRequest.FeaturetypesEnum::toString)
                        .collect(Collectors.joining(",")));

    }
}
