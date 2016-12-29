/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.test.util.database.dbunit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

/**
 * Een tool om een export te maken van een aantal tabellen; de export kan
 * vervolgens worden gebruikt in een dbunit testcase.
 *
 * @author mprins
 *
 * zie: http://dbunit.wikidot.com/demoimportexport
 */
public class DBUnitExportRSGB {

    private static final String _testDir = "src/test/resources/";
    private static final String _dbFile = "rsgb-flat.xml";
    private static final String _dbFile2 = "rsgb.xml";

    // postgis
    // private static final String _driverClass = "org.postgresql.Driver";
    // private static final String _jdbcConnection = "jdbc:postgresql://localhost:5432/mantis6098_rsgb";
    // private static final String _user = "rsgb";
    // private static final String _passwd = "rsgb";
    // ms sql
    // private static final String _driverClass = "net.sourceforge.jtds.jdbc.Driver";
    // private static final String _jdbcConnection = "jdbc:jtds:sqlserver://192.168.1.15:1433/itest_brmo_rsgb;instance=SQLEXPRESS";
    // private static final String _user = "brmotest";
    // private static final String _passwd = "brmotest";
    // oracle
    private static final String _driverClass = "oracle.jdbc.OracleDriver";
    private static final String _jdbcConnection = "jdbc:oracle:thin:@192.168.1.40:1521:db01";
    private static final String _user = "rsgbitest";
    private static final String _passwd = "rsgbitest";

    // volgorde van tabellen belangrijk vanwege de constraints
    private static final String[] _testTableNames = {
        "kad_onrrnd_zk",
        "kad_onrrnd_zk_archief",
        "kad_onrrnd_zk_his_rel",
        "kad_perceel",
        "kad_perceel_archief",
        "subject",
        "prs",
        "niet_nat_prs",
        "ingeschr_niet_nat_prs",
        "herkomst_metadata",
        "kad_onrrnd_zk_aantek",
        "kad_onrrnd_zk_aantek_archief",
        "brondocument",
        "zak_recht"
    };

    public static void main(String[] args) throws ClassNotFoundException, DatabaseUnitException, IOException, SQLException {
        Class driverClass = Class.forName(_driverClass);
        Connection jdbcConnection = DriverManager.getConnection(_jdbcConnection, _user, _passwd);
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        // connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());

        // voor alle tabellen:
        // ITableFilter filter = new DatabaseSequenceFilter(connection);
        // IDataSet dataset = new FilteredDataSet(filter, connection.createDataSet());
        // voor een setje tabellen
        IDataSet dataset = new FilteredDataSet(_testTableNames, connection.createDataSet());

        // "flat" xml export
        FlatXmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile)));
        // "formatted" xml export
        XmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile2)));
    }
}
