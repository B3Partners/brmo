/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.datamodel;

import nl.b3p.brmo.test.util.database.ViewUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Een testcase om te kijken of het upgrade script correct is verwerkt. De
 * database upgrade wordt met behulp van shell scripts gedaan, deze testcase
 * checked alleen de waarden in de metadata tabel.
 *
 * @author Mark Prins
 */
public class DatabaseUpgradeTest {

    private static final Log LOG = LogFactory.getLog(DatabaseUpgradeTest.class);
    private static String nextRelease;
    private static String previousRelease;
    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #loadProps()
     */
    private final Properties params = new Properties();
    private IDatabaseConnection db;
    private BasicDataSource ds;
    /**
     * {@code true} als we met een Oracle database bezig zijn.
     */
    private boolean isOracle;
    /**
     * {@code true} als we met een Postgis database bezig zijn.
     */
    private boolean isPostgis;

    static Stream<Arguments> localParameters() {
        return Stream.of(
                Arguments.of("staging"),
                Arguments.of("rsgb"),
                Arguments.of("rsgbbgt"),
                Arguments.of("bag"),
                Arguments.of("brk")
        );
    }

    @BeforeAll
    public static void getEnvironment() {
        nextRelease = System.getProperty("project.version").replace("-SNAPSHOT", "");
        LOG.debug("komende release is: " + nextRelease);
        //  Semantic versioning scheme (MAJOR.MINOR.PATCH)
        previousRelease = nextRelease;
        int patch = Integer.parseInt(nextRelease.substring(nextRelease.lastIndexOf(".") + 1));
        LOG.debug("release patch is: " + patch);
        previousRelease = nextRelease.substring(0, nextRelease.lastIndexOf(".")) + "." + (patch - 1);
        // HACK voor bump
        if (nextRelease.equalsIgnoreCase("2.3.0")) {
            previousRelease = "2.2.2";
        }
        LOG.debug("vorige release is: " + previousRelease);

        assumeTrue(previousRelease.matches("(\\d+\\.)(\\d+\\.)(\\d)"));
    }

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        assumeFalse(getProperty("database.properties.file") == null, "Verwacht database omgeving te zijn aangegeven.");
    }

    private void setUpDB(String dbName) throws Exception {
        this.loadProps();
        ds = new BasicDataSource();
        ds.setUrl(params.getProperty(dbName + ".url"));
        ds.setUsername(params.getProperty(dbName + ".username"));
        ds.setPassword(params.getProperty(dbName + ".password"));
        ds.setDefaultSchema(params.getProperty(dbName + ".schema"));
        ds.setAccessToUnderlyingConnectionAllowed(true);
        db = new DatabaseDataSourceConnection(ds,params.getProperty(dbName + ".schema"));

        if (this.isOracle) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            db.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegeven.");
        }
    }

    @ParameterizedTest(name = "{index}: testen database versie met: {argumentsWithNames}")
    @MethodSource("localParameters")
    public void testCurrentVersion(String dbName) throws Exception {
        setUpDB(dbName);
        ITable metadata = db.createTable("brmo_metadata");

        String waarde, naam;
        boolean foundVersion = false, foundUpdate = false;

        int rowCount = metadata.getRowCount();
        assertTrue((rowCount >= 2), "Verwacht tenminste twee records.");

        for (int i = 0; i < rowCount; i++) {
            waarde = (String) metadata.getValue(i, "waarde");
            naam = (String) metadata.getValue(i, "naam");

            LOG.debug(String.format("database %s, metadata tabel record: %d: naam: %s, waarde: %s", dbName, i, naam, waarde));

            if (nextRelease.equalsIgnoreCase(waarde)
                    && "brmoversie".equalsIgnoreCase(naam)) {
                foundVersion = true;

            }
            if (("vorige versie was " + previousRelease).equalsIgnoreCase(waarde)
                    && ("upgrade_" + previousRelease + "_naar_" + nextRelease).equalsIgnoreCase(naam)) {
                foundUpdate = true;
            }
        }
        assertTrue(foundVersion, () -> "Update versienummer niet correct voor " + dbName);

        if (!(dbName.equals("rsgbbgt") || dbName.equals("bag"))) {
            // van rsgbbgt en bag is geen vorige versie
            assertTrue(foundUpdate, () -> "Update text niet gevonden voor " + dbName);
        }
    }

    /**
     * test of de bekende set met materialized in de database(s) bestaan na upgrade.
     *
     * @throws SQLException als opzoeken in de database mislukt
     */
    @Test
    public void testBasisMViews() throws Exception {
        setUpDB("rsgb");
        List<String> viewsFound = ViewUtils.listAllMaterializedViews(ds);
        assertNotNull(viewsFound, "Geen materialized views gevonden");

        List<String> views = Arrays.asList(
                // bag
                "mb_adres",
                "mb_pand",
                "mb_benoemd_obj_adres",
                "mb_ben_obj_nevenadres",
                // brk 1
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
        assertEquals(views, viewsFound, "lijsten met materialized views zijn ongelijk");
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        LOG.info(String.format("Start: %s", testInfo.getDisplayName()));
    }

    @AfterEach
    public void cleanup() throws SQLException {
        if (ds != null) {
            db.close();
            ds.close();
        }
    }

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    private void loadProps() throws IOException {
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
        isPostgis = "postgis".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }

}
