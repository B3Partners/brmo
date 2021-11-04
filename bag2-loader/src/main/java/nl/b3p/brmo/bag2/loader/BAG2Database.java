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
import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.schema.BGTSchemaMapper;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;

/* TODO: reduce redundancy with BGTDatabase */
public class BAG2Database implements AutoCloseable {

    public enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    private SQLDialect dialect;
    private final BAG2DatabaseOptions dbOptions;
    private Connection connection;

    public BAG2Database(BAG2DatabaseOptions dbOptions) throws ClassNotFoundException {
        this.dbOptions = dbOptions;
        String connectionString = dbOptions.getConnectionString();
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
        try {
            return DriverManager.getConnection(dbOptions.getConnectionString(), dbOptions.getUser(), dbOptions.getPassword());
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

        return writer;
    }

    public static SQLDialect createDialect(BAG2Database.SQLDialectEnum dialectEnum) {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect();
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException(getMessageFormattedString("db.dialect_invalid", dialectEnum));
    }
}
