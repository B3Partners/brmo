/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.DatabaseOptions;
import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class BGTObjectTableWriterIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BGTObjectTableWriterIntegrationTest.class);

    private DatabaseOptions dbOptions = new DatabaseOptions();
    private BGTDatabase db;
    private IDatabaseTester dbTest;
    private IDatabaseConnection dbTestConnection;

    @BeforeEach
    void setUp() throws Exception {
        String connectionString = System.getProperty("db.connectionString");
        String user = System.getProperty("db.user");
        String password = System.getProperty("db.password");

        if (connectionString != null) {
            dbOptions.setConnectionString(connectionString);
        }
        if (user != null) {
            dbOptions.setUser(user);
        }
        if (password != null) {
            dbOptions.setPassword(password);
        }

        db = new BGTDatabase(dbOptions);

        dbTest = new JdbcDatabaseTester(db.getDialect().getDriverClass(), dbOptions.getConnectionString(), dbOptions.getUser(), dbOptions.getPassword());
        dbTestConnection = dbTest.getConnection();

        IDataTypeFactory dtf;
        if (db.getDialect() instanceof PostGISDialect) {
            dtf = new PostgresqlDataTypeFactory();
        } else if (db.getDialect() instanceof MSSQLDialect) {
            dtf = new MsSqlDataTypeFactory();
        } else if (db.getDialect() instanceof OracleDialect) {
            dtf = new Oracle10DataTypeFactory();
        } else {
            throw new IllegalArgumentException();
        }
        dbTestConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dtf);
        dbTestConnection.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
    }

    @AfterEach
    void tearDown() throws SQLException {
        dropTables();
        dbTestConnection.close();
    }

    private void dropTables() throws SQLException {
        try (Connection connection = dbTestConnection.getConnection()) {
            for (BGTSchema.BGTObjectType objectType: BGTSchema.getAllObjectTypes().collect(Collectors.toList())) {
                String tableName = BGTSchemaMapper.getTableNameForObjectType(objectType, "");//.toUpperCase();
                ResultSet res = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"});
                if (res.next()) {
                    try {
                        LOG.trace("Drop table: " + tableName);
                        new QueryRunner().update(connection,"drop table " + tableName);
                    } catch (SQLException se) {
                        LOG.warn("Exception dropping table " + tableName + ": " + se.getLocalizedMessage());
                    }
                }
                res.close();
            }
            if (db.getDialect() instanceof OracleDialect) {
                new QueryRunner().update(connection, "delete from user_sdo_geom_metadata");
            }
        }
    }

    @Test
    public void loadStadsdeel() throws Exception {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setIncludeHistory(true);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        writer.write(BGTTestFiles.getTestFile("bgt_stadsdeel.gml"));
        db.close();

        IDataSet dataSet = dbTestConnection.createDataSet(new String[]{"stadsdeel"});
        ITable actual = getGeomExcludedTable(dataSet.getTable("stadsdeel"));
        IDataSet expectedDataSet = new XmlDataSet(BGTTestFiles.getTestFile("bgt_stadsdeel_dataset.xml"));
        ITable expected = expectedDataSet.getTable("stadsdeel");
        Assertion.assertEquals(expected, actual);
    }

    private static ITable getGeomExcludedTable(ITable table) throws DataSetException {
        return DefaultColumnFilter.excludedColumnsTable(table, new String[] {"geom*"});
    }

    private void write(IDataSet dataSet) throws DataSetException, IOException {
        XmlDataSet.write(dataSet, System.out);
    }
}