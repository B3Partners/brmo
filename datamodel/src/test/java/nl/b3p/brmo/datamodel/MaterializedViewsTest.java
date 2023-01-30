/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * test of database versie klopt en of alle materialized views er zijn.
 */
public class MaterializedViewsTest {
    private static final Log LOG = LogFactory.getLog(MaterializedViewsTest.class);
    private static String currentVersion;
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
     * {@code true} als we met een Postgis database bezig zijn.
     */
    protected boolean isPostgis;
    private IDatabaseConnection db;
    private BasicDataSource ds;

    /**
     * init versienummer.
     */
    @BeforeAll
    public static void getEnvironment() {
        currentVersion = System.getProperty("project.version");
        LOG.debug("Werkversie is: " + currentVersion);
    }

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        assumeFalse(System.getProperty("database.properties.file") == null, "Verwacht database omgeving te zijn aangegeven.");
    }

    @BeforeEach
    public void setUpDB() throws Exception {
        this.loadProps();
        ds = new BasicDataSource();
        ds.setUrl(params.getProperty("rsgb.url"));
        ds.setUsername(params.getProperty("rsgb.username"));
        ds.setPassword(params.getProperty("rsgb.password"));
        ds.setDefaultSchema(params.getProperty("rsgb.schema"));
        ds.setAccessToUnderlyingConnectionAllowed(true);
        db = new DatabaseDataSourceConnection(ds, params.getProperty("rsgb.schema"));

        if (this.isOracle) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            db.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven.");
        }

    }

    @AfterEach
    public void cleanup() throws SQLException {
        db.close();
        ds.close();
    }

    /**
     * test of het versienummer in de database juist is.
     *
     * @throws Exception als opzoeken in de dabase mislukt
     */
    @Test
    public void testCurrentVersion() throws Exception {
        ITable metadata = db.createTable("brmo_metadata");
        int rowCount = metadata.getRowCount();
        assertTrue((rowCount >= 1), "Verwacht tenminste 1 records.");

        boolean foundVersion = false;
        String waarde, naam;
        for (int i = 0; i < rowCount; i++) {
            waarde = (String) metadata.getValue(i, "waarde");
            naam = (String) metadata.getValue(i, "naam");
            LOG.debug(String.format("rsgb metadata tabel record: %d: naam: %s, waarde: %s", i, naam, waarde));
            if ("brmoversie".equalsIgnoreCase(naam)) {
                assertEquals(currentVersion, waarde, "BRMO versienummer klopt niet");
                foundVersion = true;
            }
        }
        assertTrue(foundVersion, "Geen versienummer gevonden voor rsgb");
    }

    /**
     * test of de bekende set met materialized views in de rsgb database bestaan.
     *
     * @throws SQLException als opzoeken in de dabase mislukt
     */
    @Test
    public void testBasisMViews() throws SQLException {
        List<String> viewsFound = ViewUtils.listAllMaterializedViews(ds);
        assertNotNull(viewsFound, "Geen materialized views gevonden");

        List<String> views = Stream.of(
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
                "mb_kad_onrrnd_zk_archief",
                // bag 2
                "mb_adres_bag",
                "mb_adresseerbaar_object_geometrie_bag",
                "mb_avg_koz_rechth_bag",
                "mb_kad_onrrnd_zk_adres_bag",
                "mb_koz_rechth_bag"
        ).collect(Collectors.toList());

        if (this.isPostgis) {
            views.addAll(Arrays.asList(
                    // brk 2 / postgres
                    "brk.mb_subject",
                    "brk.mb_avg_subject",
                    "brk.mb_kad_onrrnd_zk_adres",
                    "brk.mb_percelenkaart",
                    "brk.mb_zr_rechth",
                    "brk.mb_avg_zr_rechth",
                    "brk.mb_koz_rechth",
                    "brk.mb_avg_koz_rechth",
                    "brk.mb_kad_onrrnd_zk_archief"
            ));
        }


        // alles lower-case (ORACLE!) en gesorteerd vergelijken
        viewsFound.replaceAll(String::toLowerCase);
        views.replaceAll(String::toLowerCase);
        Collections.sort(viewsFound);
        Collections.sort(views);
        assertEquals(views, viewsFound, "lijsten met materialized views zijn ongelijk");
    }


    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     * @todo naar superklasse extraheren
     */
    public void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(MaterializedViewsTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(MaterializedViewsTest.class.getClassLoader()
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
