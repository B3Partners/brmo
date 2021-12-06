/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoaderMain;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Laadt een BAG stand in de database. Test of tabellen en actueel views bestaan en gevuld zijn.
 * <i>NB</i> De anader BAG (materalized) views worden in de datamodel module getest; deze test
 * laat dus een gevulde BAG database achter.
 *
 * @author mprins
 */
public class BAGLoaderDatabaseIntegrationTest {
    private static final String[] BAGTABLES = new String[]{"ligplaats", "ligplaats_nevenadres", "nummeraanduiding", "openbareruimte", "pand", "standplaats", "standplaats_nevenadres", "verblijfsobject", "verblijfsobject_gebruiksdoel", "verblijfsobject_maaktdeeluitvan", "verblijfsobject_nevenadres", "woonplaats"};
    private static final String[] BAGACTUEELVIEWS = new String[]{"v_ligplaats_actueel", "v_nummeraanduiding_actueel", "v_openbareruimte_actueel", "v_pand_actueel", "v_standplaats_actueel", "v_verblijfsobject_actueel", "v_woonplaats_actueel"};
    private static String dbUrl = System.getProperty("dburl");
    private static String dbUser = System.getProperty("dbuser", "rsgb");
    private static String dbPass = System.getProperty("dbpassword", "rsgb");

    private BAG2Database bag2Database;
    private BAG2DatabaseOptions databaseOptions;
    private BAG2LoadOptions bag2LoadOptions;
    private IDatabaseConnection bag;
    private String testFileName;
    private String tableQualifierPrefix = "";

    @BeforeAll
    static void beforeAll() {
        BAG2LoaderMain.configureLogging(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        assumeFalse(StringUtils.isEmpty(dbUrl), "skipping integration test: missing database url");
        URL u = BAGLoaderDatabaseIntegrationTest.class.getResource("/BAGGEM1904L-15102021.zip");
        assumeFalse(null == u, "skipping integration test: missing testdata");
        testFileName = u.getFile();
        bag = new DatabaseConnection(DriverManager.getConnection(dbUrl, dbUser, dbPass));

        if (dbUrl.contains("postgresql")) {
            bag.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            bag.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
            tableQualifierPrefix = "bag.";
        }
        if (dbUrl.contains("oracle")) {
            bag.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            bag.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
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
    }

    @AfterEach
    void cleanup() throws SQLException {
        // geladen data wordt niet opgeruimd
        if (null != bag) bag.close();
    }

    @Test
    void testStand() throws SQLException {
        BAG2LoaderMain loader = new BAG2LoaderMain();

        try {
            loader.loadFiles(bag2Database, databaseOptions, bag2LoadOptions, new BAG2ProgressReporter(), new String[]{testFileName}, null);
        } catch (Exception e) {
            fail("Laden BAG data is mislukt. " + e.getLocalizedMessage(), e);
        }
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
