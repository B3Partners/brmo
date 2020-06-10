/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.datamodel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import nl.b3p.brmo.test.util.database.ViewUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Een testcase om te kijken of het upgrade script correct is verwerkt. De
 * database upgrade wordt met behulp van shell scripts gedaan, deze testcase
 * checked alleen de waarden in de metadata tabel.
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
public class DatabaseUpgradeTest {

    private static String nextRelease;
    private static String previousRelease;
    private static final Log LOG = LogFactory.getLog(DatabaseUpgradeTest.class);

    @Parameterized.Parameters(name = "{index}: testen database: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            {"staging"}, {"rsgb"}, {"rsgbbgt"}
        });
    }

    @BeforeClass
    public static void getEnvironment() {
        nextRelease = System.getProperty("project.version").replace("-SNAPSHOT", "");
        LOG.debug("komende release is: " + nextRelease);
        //  Semantic versioning scheme (MAJOR.MINOR.PATCH)
        previousRelease = nextRelease;
        int patch = Integer.parseInt(nextRelease.substring(nextRelease.lastIndexOf(".") + 1));
        LOG.debug("release patch is: " + patch);
        previousRelease = nextRelease.substring(0, nextRelease.lastIndexOf(".")) + "." + (patch - 1);
        LOG.debug("vorige release is: " + previousRelease);

        assumeTrue(previousRelease.matches("(\\d+\\.)(\\d+\\.)(\\d)"));
    }

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
    }
    private final String dbName;
    private IDatabaseConnection db;
    private BasicDataSource ds;

    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #loadProps()
     */
    protected final Properties params = new Properties();

    /**
     * {@code true} als we met een Oracle database bezig zijn.
     */
    protected boolean isOracle;

    /**
     * {@code true} als we met een MS SQL Server database bezig zijn.
     */
    protected boolean isMsSQL;

    /**
     * {@code true} als we met een Postgis database bezig zijn.
     */
    protected boolean isPostgis;

    public DatabaseUpgradeTest(String dbName) {
        this.dbName = dbName;
    }

    @Before
    public void setUpDB() throws Exception {
        this.loadProps();
        ds = new BasicDataSource();
        ds.setUrl(params.getProperty(this.dbName + ".url"));
        ds.setUsername(params.getProperty(this.dbName + ".username"));
        ds.setPassword(params.getProperty(this.dbName + ".password"));
        ds.setAccessToUnderlyingConnectionAllowed(true);
        db = new DatabaseDataSourceConnection(ds);

        if (this.isMsSQL) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            // db = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(db.getConnection()), params.getProperty(this.dbName + ".user").toUpperCase());
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            db.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven.");
        }

    }

    @Test
    public void testCurrentVersion() throws Exception {
        ITable metadata = db.createTable("brmo_metadata");

        String waarde, naam;
        boolean foundVersion = false, foundUpdate = false;

        int rowCount = metadata.getRowCount();
        assertTrue("Verwacht tenminste twee records.", (rowCount >= 2));

        for (int i = 0; i < rowCount; i++) {
            waarde = metadata.getValue(i, "waarde").toString();
            naam = metadata.getValue(i, "naam").toString();

            LOG.debug(String.format("database %s, metadata tabel record: %d: naam: %s, waarde: %s", this.dbName, i, naam, waarde));

            if (nextRelease.equalsIgnoreCase(waarde)
                    && "brmoversie".equalsIgnoreCase(naam)) {
                foundVersion = true;

            }
            if (("vorige versie was " + previousRelease).equalsIgnoreCase(waarde)
                    && ("upgrade_" + previousRelease + "_naar_" + nextRelease).equalsIgnoreCase(naam)) {
                foundUpdate = true;
            }
        }
        assertTrue("Update versienummer niet correct voor " + this.dbName, foundVersion);
        assertTrue("Update text niet gevonden voor " + this.dbName, foundUpdate);
    }

    /**
     * test of de bekende set met materialized in de database(s) bestaan na upgrade.
     *
     * @throws SQLException als opzoeken in de dabase mislukt
     */
    @Test
    public void testBasisMViews() throws SQLException {
        List<String> viewsFound = ViewUtils.listAllMaterializedViews(ds);
        assertNotNull("Geen materialized views gevonden", viewsFound);

        if (this.dbName == "rsgb") {
            if (isMsSQL) {
                // geen m-views in mssql
                assertTrue("Gek! sqlserver heeft materialized views", viewsFound.isEmpty());
            } else {
                List<String> views = Arrays.asList(
                    // bag
                    "mb_adres",
                    "mb_pand",
                    "mb_benoemd_obj_adres",
                    "mb_ben_obj_nevenadres",
                    // brk
                    "mb_subject",
                    "mb_avg_subject",
                    "mb_util_app_re_kad_perceel",
                    "mb_kad_onrrnd_zk_adres",
                    "mb_percelenkaart",
                    "mb_zr_rechth",
                    "mb_avg_zr_rechth",
                    "mb_koz_rechth",
                    "mb_avg_koz_rechth",
                    "mb_kad_onrrnd_zk_archief"
                );

                // alles lower-case (ORACLE!) en gesorteerd vergelijken
                viewsFound.replaceAll(String::toLowerCase);
                views.replaceAll(String::toLowerCase);
                Collections.sort(viewsFound);
                Collections.sort(views);
                assertEquals("lijsten met materialized views zijn ongelijk", views, viewsFound);
            }
        } else {
            assertTrue(this.dbName + "heeft materialized views", viewsFound.isEmpty());
        }
    }

    @After
    public void cleanup() throws SQLException {
        db.close();
        ds.close();
    }

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     * @todo evt naar superklasse extraheren
     */
    public void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(DatabaseUpgradeTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(DatabaseUpgradeTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
        isMsSQL = "sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));
        isPostgis = "postgis".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class stagingDriverClass = Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }

}
