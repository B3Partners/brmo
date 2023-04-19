/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.bag2.loader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoaderMain;
import nl.b3p.brmo.sql.LoggingQueryRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Laadt een BAG stand in de database. Test of tabellen en actueel views bestaan en gevuld zijn.
 * <i>NB</i> De anader BAG (materalized) views worden in de datamodel module getest; deze test laat
 * dus een gevulde BAG database achter.
 *
 * @author mprins
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BAGLoaderDatabaseIntegrationTest {
  private static final Log LOG = LogFactory.getLog(BAGLoaderDatabaseIntegrationTest.class);

  private static final String[] BAGTABLES =
      new String[] {
        "ligplaats",
        "ligplaats_nevenadres",
        "nummeraanduiding",
        "openbareruimte",
        "pand",
        "standplaats",
        "standplaats_nevenadres",
        "verblijfsobject",
        "verblijfsobject_gebruiksdoel",
        "verblijfsobject_maaktdeeluitvan",
        "verblijfsobject_nevenadres",
        "woonplaats"
      };
  private static final String[] BAGACTUEELVIEWS =
      new String[] {
        "v_ligplaats_actueel",
        "v_nummeraanduiding_actueel",
        "v_openbareruimte_actueel",
        "v_pand_actueel",
        "v_standplaats_actueel",
        "v_verblijfsobject_actueel",
        "v_woonplaats_actueel"
      };
  private static final String dbUrl = System.getProperty("dburl");
  private static final String dbUser = System.getProperty("dbuser", "rsgb");
  private static final String dbPass = System.getProperty("dbpassword", "rsgb");

  private BAG2Database bag2Database;
  private BAG2DatabaseOptions databaseOptions;
  private BAG2LoadOptions bag2LoadOptions;
  private IDatabaseConnection bag;
  private String testFileName;
  private String tableQualifierPrefix = "";
  private String schema = null;
  private String expectedXmlDataSetSuffix = "";

  @BeforeAll
  static void beforeAll() {
    BAG2LoaderMain.configureLogging(false);
  }

  @BeforeEach
  void setUp(TestInfo info) throws Exception {
    assumeFalse(StringUtils.isEmpty(dbUrl), "skipping integration test: missing database url");
    URL u = BAGLoaderDatabaseIntegrationTest.class.getResource("/BAGGEM1904L-15102021.zip");
    assumeFalse(null == u, "skipping integration test: missing testdata");
    testFileName = u.getFile();
    bag = new DatabaseConnection(DriverManager.getConnection(dbUrl, dbUser, dbPass));

    if (dbUrl.contains("postgresql")) {
      bag.getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
      bag.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
      tableQualifierPrefix = "bag.";
      schema = "bag";
    }
    if (dbUrl.contains("oracle")) {
      bag.getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
      bag.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

      expectedXmlDataSetSuffix = "-oracle";
      // If we don't set the schema to the user the DatabaseMetaData.getTables() call without
      // a schema filter will take 5 minutes
      schema = dbUser.toUpperCase();
    }

    bag2LoadOptions = new BAG2LoadOptions();
    // dit ruim alle BAG tabelle op met een cascade; dat is niet wat je wilt omdat daar tabellen
    // een views van het RSGB schema bij zitten
    // bag2LoadOptions.setDropIfExists(true);

    databaseOptions = new BAG2DatabaseOptions();
    databaseOptions.setConnectionString(dbUrl);
    databaseOptions.setUser(dbUser);
    databaseOptions.setPassword(dbPass);
    bag2Database = new BAG2Database(databaseOptions);

    if (info.getTestMethod().isPresent()) {
      if (info.getTestMethod().orElseThrow().getAnnotation(SkipDropTables.class) == null) {
        try {
          dropTables(bag.getConnection(), schema, dbUrl.contains("oracle"));
        } catch (Exception e) {
          LOG.error("Exception dropping tables before test", e);
        }
      }
    }
  }

  private static void dropTables(Connection connection, String schema, boolean isOracle)
      throws SQLException {
    LoggingQueryRunner qr = new LoggingQueryRunner();
    if (!isOracle) {
      LOG.trace("Drop BAG schema");
      qr.update(connection, "drop schema if exists " + schema + " cascade");
    } else {
      IMetadataHandler metadataHandler = new DefaultMetadataHandler();
      try (ResultSet tablesRs =
          metadataHandler.getTables(connection.getMetaData(), schema, new String[] {"TABLE"})) {
        while (tablesRs.next()) {
          String tableName = tablesRs.getString("TABLE_NAME");
          try {
            LOG.trace("Drop table: " + tableName);
            qr.update(connection, "drop table " + tableName + " cascade constraints");
          } catch (SQLException se) {
            LOG.warn("Exception dropping table " + tableName + ": " + se.getLocalizedMessage());
          }
        }
      }
      try {
        qr.update(connection, "drop sequence objectid_seq");
      } catch (Exception ignored) {
      }
      qr.update(connection, "delete from user_sdo_geom_metadata");
    }
  }

  @AfterEach
  void cleanup() throws SQLException {
    if (null != bag) bag.close();
  }

  private void loadBAGResourceFile(String file) {
    try {
      BAG2LoaderMain loader = new BAG2LoaderMain();
      loader.loadFiles(
          bag2Database,
          databaseOptions,
          bag2LoadOptions,
          new BAG2ProgressReporter(),
          new String[] {file},
          null);
    } catch (Exception e) {
      fail("Laden BAG data uit resource " + file + " is mislukt: " + e.getLocalizedMessage(), e);
    }
  }

  private void compareDataSet(String[] tables, String expectedXmlDataSetFileName) throws Exception {
    IDatabaseTester databaseTester =
        new JdbcDatabaseTester(
            bag2Database.getDialect().getDriverClass(),
            databaseOptions.getConnectionString(),
            databaseOptions.getUser(),
            databaseOptions.getPassword(),
            schema);
    IDatabaseConnection dbTestConnection = databaseTester.getConnection();
    IDataSet actualDataSet = dbTestConnection.createDataSet(tables);
    if (System.getProperty("db.writeActualDataSet") != null) {
      XmlDataSet.write(actualDataSet, System.out);
    }
    IDataSet expectedDataSet =
        new XmlDataSet(
            Objects.requireNonNull(
                    BAGLoaderDatabaseIntegrationTest.class.getResource(
                        String.format(
                            "/expected/%s%s.xml",
                            expectedXmlDataSetFileName, expectedXmlDataSetSuffix)))
                .openStream());
    for (String table : tables) {
      Assertion.assertEqualsIgnoreCols(
          expectedDataSet, actualDataSet, table, new String[] {"objectid"});
    }
  }

  private static String getResourceFile(String resource) {
    return Objects.requireNonNull(BAGLoaderDatabaseIntegrationTest.class.getResource(resource))
        .getFile();
  }

  @Test
  @Order(1)
  void testCompareDataSetWoonplaatsStand() throws Exception {
    loadBAGResourceFile(getResourceFile("/BAGGEM3502L-15102021.zip"));
    compareDataSet(new String[] {"woonplaats"}, "bag2-woonplaats-stand");
  }

  @Test
  @Order(2)
  @SkipDropTables
  void testCompareDataSetWoonplaatsMutatie() throws Exception {
    loadBAGResourceFile(getResourceFile("/BAGNLDM-23052022-24052022.zip"));
    compareDataSet(new String[] {"woonplaats"}, "bag2-woonplaats-gemuteerd");
  }

  // Leave this as last integration test case, so the BAG tables loaded by it can be used to test
  // the create view scripts
  @Test
  @Order(Integer.MAX_VALUE)
  void testStandAllTablesAndViewsHaveRows() throws Exception {
    loadBAGResourceFile(testFileName);

    // check tables
    for (String t : BAGTABLES) {
      // omdat sommige BAG tabellen ook in RSGB schema zitten bag qualifier gebruiken
      t = tableQualifierPrefix + t;
      assertTrue(bag.getRowCount(t) > 0, "Onverwacht lege tabel: " + t);
    }
    // check views
    for (String t : BAGACTUEELVIEWS) {
      assertTrue(bag.getRowCount(t) > 0, "Onverwacht lege view: " + t);
    }
  }
}
