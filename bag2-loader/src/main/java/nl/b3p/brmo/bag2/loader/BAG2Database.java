/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.bag2.schema.BAG2SchemaMapper;
import nl.b3p.brmo.sql.LoggingQueryRunner;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bag2.loader.BAG2LoaderUtils.getBundleString;
import static nl.b3p.brmo.bag2.loader.BAG2LoaderUtils.getMessageFormattedString;
import static nl.b3p.brmo.bag2.schema.BAG2SchemaMapper.METADATA_TABLE_NAME;

/* TODO: reduce redundancy with BGTDatabase, remove dependency on dialect CLI enum */
public class BAG2Database implements AutoCloseable {
    private static final Log LOG = LogFactory.getLog(BAG2Database.class);

    public enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    private SQLDialect dialect;
    private final BAG2DatabaseOptions dbOptions;
    private Connection connection;
    private final boolean allowConnectionCreation;

    public BAG2Database(BAG2DatabaseOptions dbOptions) throws ClassNotFoundException {
        this.dbOptions = dbOptions;
        dialect = createDialect(dbOptions.getConnectionString());
        dialect.loadDriver();
        this.allowConnectionCreation = true;
    }

    public BAG2Database(BAG2DatabaseOptions dbOptions, Connection connection) throws SQLException {
        this.dbOptions = dbOptions;
        this.connection = connection;
        dialect = createDialect(connection.getMetaData().getURL());
        this.allowConnectionCreation = false;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public void setDialect(SQLDialect dialect){
        this.dialect = dialect;
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = createConnection();
        }
        return this.connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
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
            Connection c =  DriverManager.getConnection(dbOptions.getConnectionString(), dbOptions.getUser(), dbOptions.getPassword());

            if (dialect instanceof PostGISDialect) {
                new QueryRunner().update(c, "create schema if not exists bag");
                new QueryRunner().update(c, "set search_path=bag,public");
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(getMessageFormattedString("db.connection_error", dbOptions.getConnectionString()), e);
        }
    }

    public BAG2ObjectTableWriter createObjectTableWriter(BAG2LoadOptions loadOptions, BAG2DatabaseOptions dbOptions) throws SQLException {
        BAG2ObjectTableWriter writer = new BAG2ObjectTableWriter(getConnection(), this.getDialect(), BAG2SchemaMapper.getInstance());

        if (loadOptions == null) {
            loadOptions = new BAG2LoadOptions();
        }
        writer.setBatchSize(dbOptions.getBatchSize() != null ? dbOptions.getBatchSize() : this.getDialect().getDefaultOptimalBatchSize());
        writer.setMultithreading(loadOptions.isMultithreading());
        writer.setUsePgCopy(dbOptions.isUsePgCopy());
        writer.setObjectLimit(loadOptions.getMaxObjects());
        writer.setIgnoreDuplicates(loadOptions.isIgnoreDuplicates());
        writer.setDropIfExists(loadOptions.isDropIfExists());

        return writer;
    }

    public static SQLDialect createDialect(String connectionString) {
        BAG2Database.SQLDialectEnum sqlDialectEnum;
        if (connectionString.startsWith("jdbc:postgresql:")) {
            sqlDialectEnum = BAG2Database.SQLDialectEnum.postgis;
        } else if (connectionString.startsWith("jdbc:oracle:thin:")) {
            sqlDialectEnum = BAG2Database.SQLDialectEnum.oracle;
        } else if (connectionString.startsWith("jdbc:sqlserver:")) {
            sqlDialectEnum = BAG2Database.SQLDialectEnum.mssql;
        } else {
            throw new IllegalArgumentException(getMessageFormattedString("db.unknown_connection_string_dialect", connectionString));
        }
        return createDialect(sqlDialectEnum);
    }

    public static SQLDialect createDialect(BAG2Database.SQLDialectEnum dialectEnum) {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect();
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException(getMessageFormattedString("db.dialect_invalid", dialectEnum));
    }

    public void createMetadataTable(BAG2LoadOptions loadOptions) throws SQLException {
        LOG.info(getBundleString("db.create_metadata"));
        for(String sql: BAG2SchemaMapper.getInstance().getCreateMetadataTableStatements(getDialect(), "", loadOptions.isDropIfExists())) {
            new LoggingQueryRunner().update(getConnection(), sql);
        }
    }

    public String getMetadata(BAG2SchemaMapper.Metadata key) throws SQLException {
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

    public void setMetadataValue(BAG2SchemaMapper.Metadata key, String value) throws Exception {
        try {
            int updated = new LoggingQueryRunner().update(getConnection(), "update " + METADATA_TABLE_NAME + " set waarde = ? where naam = ?", value, key.getDbKey());
            if (updated == 0) {
                new LoggingQueryRunner().update(getConnection(), "insert into " + METADATA_TABLE_NAME + "(naam, waarde) values(?,?)", key.getDbKey(), value);
            }
        } catch (SQLException e) {
            throw new Exception(getMessageFormattedString("db.metadata_error", key.getDbKey(), value, e.getMessage()), e);
        }
    }

    public LocalDate getCurrentTechnischeDatum() throws SQLException {
        String s = getMetadata(BAG2SchemaMapper.Metadata.CURRENT_TECHNISCHE_DATUM);
        if (s == null) {
            throw new IllegalStateException("Geen huidige BAG2 stand ingeladen");
        }
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public Set<String> getGemeenteCodes() throws SQLException {
        String s = getMetadata(BAG2SchemaMapper.Metadata.GEMEENTE_CODES);
        if (s == null) {
            throw new IllegalStateException("Geen huidige BAG2 stand voor gemeentes ingeladen");
        }
        return Arrays.stream(s.split(",")).collect(Collectors.toSet());
    }
}
