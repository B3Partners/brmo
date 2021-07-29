/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.DatabaseOptions;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBTestBase {
    private static final Log LOG = LogFactory.getLog(DBTestBase.class);

    protected DatabaseOptions dbOptions = new DatabaseOptions();
    protected BGTDatabase db;
    protected IDatabaseTester dbTest;
    protected IDatabaseConnection dbTestConnection;
    protected String schema = null;
    protected String datasetFileSuffix;
    protected String connectionString;
    protected String user;
    protected String password;

    String getConfig(String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName);
        return value == null ? defaultValue : value;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Get database connection options from test profile
        connectionString = getConfig("db.connectionString", dbOptions.getConnectionString());
        user = getConfig("db.user", dbOptions.getUser());
        password = getConfig("db.password", dbOptions.getPassword());

        dbOptions.setConnectionString(connectionString);
        dbOptions.setUser(user);
        dbOptions.setPassword(password);

        db = new BGTDatabase(dbOptions);

        if (db.getDialect() instanceof OracleDialect) {
            // If we don't set the user the DatabaseMetaData.getTables() call without a schema filter will take 5 minutes
            schema = dbOptions.getUser().toUpperCase();
        }
        dbTest = new JdbcDatabaseTester(db.getDialect().getDriverClass(), dbOptions.getConnectionString(), dbOptions.getUser(), dbOptions.getPassword(), schema);
        dbTestConnection = dbTest.getConnection();

        IDataTypeFactory dtf;
        if (db.getDialect() instanceof PostGISDialect) {
            dtf = new PostgresqlDataTypeFactory();
            datasetFileSuffix = "_postgresql.xml";
        } else if (db.getDialect() instanceof MSSQLDialect) {
            dtf = new MsSqlDataTypeFactory();
            datasetFileSuffix = "_mssql.xml";
        } else if (db.getDialect() instanceof OracleDialect) {
            dtf = new Oracle10DataTypeFactory();
            datasetFileSuffix = "_oracle.xml";
        } else {
            throw new IllegalArgumentException();
        }
        dbTestConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dtf);
        dbTestConnection.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
    }

    protected void assertDataSetEquals(String tables, String dataSetXmlFiles) throws DatabaseUnitException, SQLException, IOException {
        IDataSet actualDataSet = dbTestConnection.createDataSet(tables.split(","));
        if (System.getProperty("db.writeActualDataSet") != null) {
            write(actualDataSet);
        }
        List<IDataSet> dataSets = new ArrayList<>();
        for(String dataSetXmlFile: dataSetXmlFiles.split(",")) {
            dataSets.add(new XmlDataSet(BGTTestFiles.getTestInputStream("expected/" + dataSetXmlFile + datasetFileSuffix)));
        }
        IDataSet expectedDataSet = new CompositeDataSet(dataSets.toArray(new IDataSet[]{}));
        Assertion.assertEquals(expectedDataSet, actualDataSet);
    }

    private void write(IDataSet dataSet) throws DataSetException, IOException {
        XmlDataSet.write(dataSet, System.out);
    }

    @AfterEach
    void tearDown() throws SQLException {
        dropTables();
        dbTestConnection.close();
    }

    private void dropTables() throws SQLException {
        try (Connection connection = dbTestConnection.getConnection()) {
            for (String tableName: Stream.concat(
                    BGTSchema.getAllObjectTypes().map(objectType -> BGTSchemaMapper.getTableNameForObjectType(objectType, "")),
                    Stream.of(BGTSchemaMapper.METADATA_TABLE)).collect(Collectors.toList())) {
                if (db.getDialect() instanceof OracleDialect) {
                    tableName = tableName.toUpperCase();
                }
                if(new DefaultMetadataHandler().tableExists(connection.getMetaData(), schema, tableName)) {
                    try {
                        LOG.trace("Drop table: " + tableName);
                        new QueryRunner().update(connection,"drop table " + tableName);
                    } catch (SQLException se) {
                        LOG.warn("Exception dropping table " + tableName + ": " + se.getLocalizedMessage());
                    }
                }
            }
            if (db.getDialect() instanceof OracleDialect) {
                new QueryRunner().update(connection, "delete from user_sdo_geom_metadata");
            }
        }
    }
}
