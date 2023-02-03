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
import org.dbunit.database.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

/**
 * Een tool om een export te maken van een aantal tabellen; de export kan vervolgens worden gebruikt
 * in een dbunit testcase.
 *
 * @author mprins
 *     <p>zie: http://dbunit.wikidot.com/demoimportexport
 */
public class DBUnitExportStaging {

  private static final String _testDir = "target/";
  private static final String _dbFile = "staging-flat.xml";
  private static final String _dbFile2 = "staging.xml";

  // postgis
  private static final String _driverClass = "org.postgresql.Driver";
  private static final String _jdbcConnection = "jdbc:postgresql://localhost:5432/staging";
  private static final String _user = "rsgb";
  private static final String _passwd = "rsgb";

  // oracle
  // private static final String _driverClass = "oracle.jdbc.OracleDriver";
  // private static final String _jdbcConnection = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
  // private static final String _user = "stagingitest";
  // private static final String _passwd = "stagingitest";
  // volgorde van tabellen belangrijk vanwege de constraint op laadproces-id
  private static final String[] _testTableNames = {"laadproces" /*, "bericht"*/};

  public static void main(String[] args)
      throws ClassNotFoundException, DatabaseUnitException, IOException, SQLException {
    Class driverClass = Class.forName(_driverClass);
    Connection jdbcConnection = DriverManager.getConnection(_jdbcConnection, _user, _passwd);
    IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
    // postgis
    connection
        .getConfig()
        .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    // oracle
    //        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection,
    // _user.toUpperCase());
    //        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new
    // Oracle10DataTypeFactory());
    //
    // connection.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES,
    // true);
    IDataSet dataset;

    // voor een query
    dataset = new QueryDataSet(connection);
    // ((QueryDataSet)dataset).addTable("bericht", "SELECT
    // id,br_orgineel_xml,br_xml,datum,job_id,object_ref,soort,status,status_datum,volgordenummer,xsl_version,laadprocesid FROM bericht WHERE object_ref='NL.KAD.OnroerendeZaak:20930170970000'");
    ((QueryDataSet) dataset)
        .addTable(
            "laadproces",
            "SELECT id,contractafgiftenummer,contractnummer,klantafgiftenummer FROM laadproces WHERE id>50 order by id");

    // voor alle tabellen:
    // ITableFilter filter = new DatabaseSequenceFilter(connection);
    //        dataset = new FilteredDataSet(filter, connection.createDataSet());

    // voor de set tabellen
    //         dataset = new FilteredDataSet(_testTableNames, connection.createDataSet());

    // "flat" xml export
    FlatXmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile)));
    // "formatted" xml export
    XmlDataSet.write(dataset, new FileOutputStream(new File(_testDir, _dbFile2)));
  }
}
