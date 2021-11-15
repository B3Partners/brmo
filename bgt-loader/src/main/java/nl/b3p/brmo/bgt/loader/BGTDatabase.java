/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.cli.DatabaseOptions;
import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.schema.BGTSchemaMapper;
import nl.b3p.brmo.sql.LoggingQueryRunner;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bgt.schema.BGTSchemaMapper.METADATA_TABLE_NAME;
import static nl.b3p.brmo.bgt.schema.BGTSchemaMapper.Metadata;
import static nl.b3p.brmo.bgt.loader.Utils.getBundleString;
import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

public class BGTDatabase implements AutoCloseable {
    private static final Log log = LogFactory.getLog(BGTDatabase.class);

    public enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    private SQLDialect dialect;
    private final DatabaseOptions dbOptions;
    private Connection connection;
    private final boolean allowConnectionCreation;

    /**
     * Create a BGTDatabase that will load the driver and create a database connection.
     * @param dbOptions Database connection options.
     * @throws ClassNotFoundException If the JDBC driver could not be loaded.
     */
    public BGTDatabase(DatabaseOptions dbOptions) throws ClassNotFoundException {
        this.dbOptions = dbOptions;
        dialect = createDialect(dbOptions.getConnectionString());
        dialect.loadDriver();
        this.allowConnectionCreation = true;
    }

    /**
     * Create a BGTDatabase with a supplied Connection without loading the driver and making connections here. The
     * connection string in DatabaseOptions is not used, it is retrieved from the supplied Connection to determine the
     * dialect.
     * @param dbOptions Database options - not used for making connections
     * @param connection Connection to use, must not be closed. Connection will be closed by CLI classes after processing,
     *                  override the close() method to avoid that.
     */
    public BGTDatabase(DatabaseOptions dbOptions, Connection connection) throws SQLException {
        this.dbOptions = dbOptions;
        this.dialect = createDialect(connection.getMetaData().getURL());
        this.connection = connection;
        this.allowConnectionCreation = false;
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

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    public void setDialect(SQLDialect dialect){
        this.dialect = dialect;
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

    private Connection createConnection() {
        if (!allowConnectionCreation) {
            throw new RuntimeException("New connection required but supplied connection is null or closed");
        }
        try {
            return DriverManager.getConnection(dbOptions.getConnectionString(), dbOptions.getUser(), dbOptions.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(getMessageFormattedString("db.connection_error", dbOptions.getConnectionString()), e);
        }
    }

    public BGTObjectTableWriter createObjectTableWriter(LoadOptions loadOptions, DatabaseOptions dbOptions) throws SQLException {
        BGTObjectTableWriter writer = new BGTObjectTableWriter(getConnection(), this.getDialect(), BGTSchemaMapper.getInstance());

        if (loadOptions == null) {
            loadOptions = new LoadOptions();
        }
        writer.setBatchSize(dbOptions.getBatchSize() != null ? dbOptions.getBatchSize() : this.getDialect().getDefaultOptimalBatchSize());
        writer.setMultithreading(loadOptions.isMultithreading());
        writer.setUsePgCopy(dbOptions.isUsePgCopy());
        writer.setObjectLimit(loadOptions.getMaxObjects());
        writer.setLinearizeCurves(loadOptions.isLinearizeCurves());
        writer.setCurrentObjectsOnly(!loadOptions.isIncludeHistory());
        writer.setCreateSchema(loadOptions.isCreateSchema());
        writer.setDropIfExists(loadOptions.isDropIfExists());
        writer.setTablePrefix(loadOptions.getTablePrefix());
        return writer;
    }

    public static SQLDialect createDialect(String connectionString) {
        SQLDialectEnum sqlDialectEnum;
        if (connectionString.startsWith("jdbc:postgresql:")) {
            sqlDialectEnum = SQLDialectEnum.postgis;
        } else if (connectionString.startsWith("jdbc:oracle:thin:")) {
            sqlDialectEnum = SQLDialectEnum.oracle;
        } else if (connectionString.startsWith("jdbc:sqlserver:")) {
            sqlDialectEnum = SQLDialectEnum.mssql;
        } else {
            throw new IllegalArgumentException(getMessageFormattedString("db.unknown_connection_string_dialect", connectionString));
        }
        return createDialect(sqlDialectEnum);
    }

    public static SQLDialect createDialect(SQLDialectEnum dialectEnum) {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect();
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException(getMessageFormattedString("db.dialect_invalid", dialectEnum));
    }

    public void createMetadataTable(LoadOptions loadOptions) throws SQLException {
        log.info(getBundleString("db.create_metadata"));
        for(String sql: BGTSchemaMapper.getInstance().getCreateMetadataTableStatements(getDialect(), loadOptions.getTablePrefix(), loadOptions.isDropIfExists())) {
            new LoggingQueryRunner().update(getConnection(), sql);
        }
    }

    public String getMetadata(Metadata key) throws SQLException {
        Object value = new LoggingQueryRunner().query(getConnection(), "select waarde from " + METADATA_TABLE_NAME + " where naam = ?", new ScalarHandler<>(), key.getDbKey());
        if (value == null) {
            return null;
        }
        if (value instanceof Clob) {
            Clob clob = (Clob)value;
            return clob.getSubString(1, (int)clob.length());
        }
        return value.toString();
    }

    public void setMetadataValue(Metadata key, String value) throws Exception {
        try {
            int updated = new LoggingQueryRunner().update(getConnection(), "update " + METADATA_TABLE_NAME + " set waarde = ? where naam = ?", value, key.getDbKey());
            if (updated == 0) {
                new LoggingQueryRunner().update(getConnection(), "insert into " + METADATA_TABLE_NAME + "(naam, waarde) values(?,?)", key.getDbKey(), value);
            }
        } catch (SQLException e) {
            throw new Exception(getMessageFormattedString("db.metadata_error", key.getDbKey(), value, e.getMessage()), e);
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
