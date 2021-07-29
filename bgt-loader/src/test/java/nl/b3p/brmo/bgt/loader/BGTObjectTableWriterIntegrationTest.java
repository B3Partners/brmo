/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.DatabaseOptions;
import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatDtdWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.dataset.xml.XmlDataSetWriter;
import org.geotools.util.logging.Logging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

public class BGTObjectTableWriterIntegrationTest {
    private DatabaseOptions dbOptions = new DatabaseOptions();
    private BGTDatabase db;
    private IDatabaseTester dbTest;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
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
    }

    @Test
    public void loadStadsdeel() throws Exception {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setIncludeHistory(true);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        writer.write(BGTTestFiles.getTestFile("bgt_stadsdeel.gml"));

        IDataSet dataSet = dbTest.getConnection().createDataSet(new String[]{"stadsdeel"});
        ITable actual = dataSet.getTable("stadsdeel");
        IDataSet expectedDataSet = new XmlDataSet(BGTTestFiles.getTestFile("bgt_stadsdeel_dataset.xml"));
        ITable expected = expectedDataSet.getTable("stadsdeel");
        Assertion.assertEquals(expected, actual);
    }

    private void write(IDataSet dataSet) throws DataSetException, IOException {
        XmlDataSet.write(dataSet, System.out);
    }
}