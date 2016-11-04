/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;

/**
 * Een tool om een export te maken van een aantal tabellen; de export kan
 * vervolgens worden gebruikt in een dbunit testcase.
 *
 * @see Mantis6098IntegrationTest#setUp()
 * @author mprins
 *
 * zie: http://dbunit.wikidot.com/demoimportexport
 */
public class DBUnitExport {

    //private static final String _testDir = "src/test/resources/mantis6098/";
    private static final String _testDir = "src/test/resources/";
    private static final String _dbFile = "gevuld-staging-flat.xml";
    private static final String _dbFile2 = "gevuld-staging.xml";
    private static final String _driverClass = "org.postgresql.Driver";
    //private static final String _jdbcConnection = "jdbc:postgresql://localhost:5432/mantis6098";
    //private static final String _user = "rsgb";
    //private static final String _passwd = "rsgb";
    private static final String _jdbcConnection = "jdbc:jtds:sqlserver://192.168.1.15:1433/itest_brmo_staging;instance=SQLEXPRESS";
    private static final String _user = "brmotest";
    private static final String _passwd = "brmotest";
    // volgorde van tabellen belangrijk vanwege constraint op laadproces-id
    private static final String[] _testTableNames = {"laadproces", "bericht2"};

    public static void main(String[] args) throws ClassNotFoundException, DatabaseUnitException, IOException, SQLException {
        Class driverClass = Class.forName(_driverClass);
        Connection jdbcConnection = DriverManager.getConnection(_jdbcConnection, _user, _passwd);
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);;

        // voor alle tabellen:
        // ITableFilter filter = new DatabaseSequenceFilter(connection);
        // IDataSet dataset = new FilteredDataSet(filter, connection.createDataSet());
        // voor een setje tabellen
        IDataSet dataset = new FilteredDataSet(_testTableNames, connection.createDataSet());
        // "flat" xml
        FlatXmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile)));
        // "big" xml
        XmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile2)));
    }
}
