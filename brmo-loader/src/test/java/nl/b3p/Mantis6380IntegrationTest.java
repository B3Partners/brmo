/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testcases voor mantis-6380 / GH#332; incorrect verwerken van commanditair
 * vennootschap rechten in mutatie. Draaien met:
 * {@code mvn -Dit.test=Mantis6380IntegrationTest -Dtest.onlyITs=true verify -Poracle -pl :brmo-loader > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=Mantis6380IntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl :brmo-loader > /tmp/postgresql.log}.
 *
 * @author mprins
 */
@Tag("skip-windows-java11")
public class Mantis6380IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6380IntegrationTest.class);

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();
    private BasicDataSource dsRsgb;
    private BasicDataSource dsStaging;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        rsgb = new DatabaseDataSourceConnection(dsRsgb, params.getProperty("rsgb.schema"));
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isOracle) {
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6380IntegrationTest.class.getResource("/mantis6380/staging.xml").toURI())));
        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        brmo = new BrmoFramework(dsStaging, dsRsgb, null);

        // skip als de bron data er niet is
        Assumptions.assumeTrue(2L == brmo.getCountBerichten("brk", "STAGING_OK"),
                "Er zijn geen 2 STAGING_OK berichten");
        Assumptions.assumeTrue(2L == brmo.getCountLaadProcessen("brk", "STAGING_OK"),
                "Er zijn geen 2 STAGING_OK laadproces");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();
        dsRsgb.close();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        dsStaging.close();

        sequential.unlock();
    }

    @Test
    public void testTransformStand() throws Exception {
        brmo.setOrderBerichten(false);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(
                1L, brmo.getCountBerichten("brk", "STAGING_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        assertEquals(
                1L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
        assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                "Er zijn berichten met status RSGB_NOK");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Aantal actuele onroerende zaken is incorrect");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Aantal actuele percelen is incorrect");
        assertEquals("37640054270000", kad_perceel.getValue(0, "sc_kad_identif").toString(),
                "Perceel identif is incorrect");

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal subjecten klopt niet");

        ITable ingeschr_niet_nat_prs = rsgb.createDataSet().getTable("ingeschr_niet_nat_prs");
        assertEquals(1, ingeschr_niet_nat_prs.getRowCount(), "Aantal ingeschr_niet_nat_prs klopt niet");

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(2, zak_recht.getRowCount(), "Aantal zakelijke rechten klopt niet");
        // rij 1
        assertEquals("NL.KAD.Tenaamstelling.AKR1.100000007757634", zak_recht.getValue(0, "kadaster_identif").toString());
        assertEquals("1", zak_recht.getValue(0, "ar_noemer").toString());
        assertEquals("1", zak_recht.getValue(0, "ar_teller").toString());
        // rij 2
        assertEquals("NL.KAD.ZakelijkRecht.AKR1.100000007757634", zak_recht.getValue(1, "kadaster_identif").toString());
        assertNull(zak_recht.getValue(1, "ar_noemer"), "noemer is niet null");
        assertNull(zak_recht.getValue(1, "ar_teller"), "teller is niet null");
    }

    /**
     * transformeer alle berichten en test of dat correct is gedaan.
     *
     * @throws Exception if any
     */
    @Test
    public void testTransformBerichten() throws Exception {
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(
                0L, brmo.getCountBerichten("brk", "STAGING_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        assertEquals(
                2L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
        assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                "Er zijn berichten met status RSGB_NOK");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue(brondocument.getRowCount() > 0, "Er zijn geen brondocumenten");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Aantal actuele onroerende zaken is incorrect");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Aantal actuele percelen is incorrect");
        assertEquals("37640054270000", kad_perceel.getValue(0, "sc_kad_identif").toString(),
                "Perceel identif is incorrect");

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(3, subject.getRowCount(), "Aantal subjecten klopt niet");

        ITable ingeschr_niet_nat_prs = rsgb.createDataSet().getTable("ingeschr_niet_nat_prs");
        assertEquals(3, ingeschr_niet_nat_prs.getRowCount(), "Aantal ingeschr_niet_nat_prs klopt niet");

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(3, zak_recht.getRowCount(), "Aantal zakelijke rechten klopt niet");
        // rij 1
        assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798492", zak_recht.getValue(0, "kadaster_identif").toString());
        assertEquals("6", zak_recht.getValue(0, "ar_teller").toString());
        assertEquals("25", zak_recht.getValue(0, "ar_noemer").toString());
        // rij 2
        assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798558", zak_recht.getValue(1, "kadaster_identif").toString());
        assertEquals("19", zak_recht.getValue(1, "ar_teller").toString());
        assertEquals("25", zak_recht.getValue(1, "ar_noemer").toString());
        // rij 3
        assertEquals("NL.KAD.ZakelijkRecht.AKR1.100000007757634", zak_recht.getValue(2, "kadaster_identif").toString());
        assertNull(zak_recht.getValue(2, "ar_teller"), "teller is niet null");
        assertNull(zak_recht.getValue(2, "ar_noemer"), "noemer is niet null");
    }

    /**
     * transformeer eerst stand bericht en daarna mutatie bericht en test of dat
     * correct is gedaan.
     *
     * @throws Exception if any
     */
    @Test
    public void testTransformStandDaarnaMutatieBerichten() throws Exception {
        brmo.setOrderBerichten(false);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(
                1L, brmo.getCountBerichten("brk", "STAGING_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        assertEquals(
                1L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
        assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                "Er zijn berichten met status RSGB_NOK");

        brmo.setOrderBerichten(true);
        t = brmo.toRsgb();
        t.join();

        assertEquals(
                0L, brmo.getCountBerichten("brk", "STAGING_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        assertEquals(
                2L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
        assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                "Er zijn berichten met status RSGB_NOK");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue(brondocument.getRowCount() > 0, "Er zijn geen brondocumenten");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Aantal actuele onroerende zaken is incorrect");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Aantal actuele percelen is incorrect");
        assertEquals("37640054270000", kad_perceel.getValue(0, "sc_kad_identif").toString(),
                "Perceel identif is incorrect");

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(3, subject.getRowCount(), "Aantal subjecten klopt niet");

        ITable ingeschr_niet_nat_prs = rsgb.createDataSet().getTable("ingeschr_niet_nat_prs");
        assertEquals(3, ingeschr_niet_nat_prs.getRowCount(), "Aantal ingeschr_niet_nat_prs klopt niet");

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(3, zak_recht.getRowCount(), "Aantal zakelijke rechten klopt niet");
        // rij 1
        assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798492", zak_recht.getValue(0, "kadaster_identif"
        ).toString());
        assertEquals("6", zak_recht.getValue(0, "ar_teller").toString());
        assertEquals("25", zak_recht.getValue(0, "ar_noemer").toString());
        // rij 2
        assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798558", zak_recht.getValue(1, "kadaster_identif").toString());
        assertEquals("19", zak_recht.getValue(1, "ar_teller").toString());
        assertEquals("25", zak_recht.getValue(1, "ar_noemer").toString());
        // rij 3
        assertEquals("NL.KAD.ZakelijkRecht.AKR1.100000007757634", zak_recht.getValue(2, "kadaster_identif").toString());
        assertNull(zak_recht.getValue(2, "ar_teller"), "teller is niet null");
        assertNull(zak_recht.getValue(2, "ar_noemer"), "noemer is niet null");
    }

    /**
     * transformeer eerst stand bericht, verwijder dat dan en transformeer
     * daarna mutatie bericht en test of dat correct is gedaan. Verwacht een
     * AssertionError omdat het aantal zakelijk rechten dan niet correct is
     * (het oude zakelijk recht wordt niet opgeruimd).
     *
     */
    @Test
    public void testTransformDeleteStandDaarnaMutatieBerichten() {
        assertThrows(AssertionError.class, ()->{
            brmo.setOrderBerichten(false);
            Thread t = brmo.toRsgb();
            t.join();

            assertEquals(
                    1L, brmo.getCountBerichten("brk", "STAGING_OK"),
                    "Niet alle berichten zijn OK getransformeerd");
            assertEquals(
                    1L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
            assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                    "Er zijn berichten met status RSGB_NOK");

            // delete stand bericht
            brmo.delete(1L);

            assertEquals(
                    1L, brmo.getCountBerichten("brk", "STAGING_OK"),
                    "Niet alle berichten zijn OK getransformeerd");
            assertEquals(
                    0L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
            assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                    "Er zijn berichten met status RSGB_NOK");

            // transformeer mutatie
            brmo.setOrderBerichten(true);
            t = brmo.toRsgb();
            t.join();

            assertEquals(
                    0L, brmo.getCountBerichten("brk", "STAGING_OK"),
                    "Niet alle berichten zijn OK getransformeerd");
            assertEquals(
                    1L, brmo.getCountBerichten("brk", "RSGB_OK"), "Niet alle berichten zijn OK getransformeerd");
            assertEquals(0L, brmo.getCountBerichten("brk", "RSGB_NOK"),
                    "Er zijn berichten met status RSGB_NOK");

            ITable brondocument = rsgb.createDataSet().getTable("brondocument");
            assertTrue(brondocument.getRowCount() > 0, "Er zijn geen brondocumenten");

            ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
            assertEquals(1, kad_onrrnd_zk.getRowCount(), "Aantal actuele onroerende zaken is incorrect");

            ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
            assertEquals(1, kad_perceel.getRowCount(), "Aantal actuele percelen is incorrect");
            assertEquals("37640054270000", kad_perceel.getValue(0, "sc_kad_identif").toString(),
                    "Perceel identif is incorrect");

            ITable subject = rsgb.createDataSet().getTable("subject");
            assertEquals(3, subject.getRowCount(), "Aantal subjecten klopt niet");

            ITable ingeschr_niet_nat_prs = rsgb.createDataSet().getTable("ingeschr_niet_nat_prs");
            assertEquals(3, ingeschr_niet_nat_prs.getRowCount(), "Aantal ingeschr_niet_nat_prs klopt niet");

            ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");

            // fail (met AssertionError), want als het stand bericht is verdwenen is er geen mogelijkheid meer om
            // te achterhalen wat opgeruimd moet worden en wordt de mutatie als stand/nieuw object verwerkt.
            // De oude rechten blijven dan aanwezig - dus 3 + 1 oude
            assertEquals(3, zak_recht.getRowCount(), "Aantal zakelijke rechten klopt niet");

            // rij 1
            assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798492", zak_recht.getValue(0, "kadaster_identif").toString());
            assertEquals("6", zak_recht.getValue(0, "ar_teller").toString());
            assertEquals("25", zak_recht.getValue(0, "ar_noemer").toString());
            // rij 2
            assertEquals("NL.KAD.Tenaamstelling.AKR1.100000012798558", zak_recht.getValue(1, "kadaster_identif").toString());
            assertEquals("19", zak_recht.getValue(1, "ar_teller").toString());
            assertEquals("25", zak_recht.getValue(1, "ar_noemer").toString());
            // rij 3
            assertEquals("NL.KAD.ZakelijkRecht.AKR1.100000007757634", zak_recht.getValue(2, "kadaster_identif").toString());
            assertNull(zak_recht.getValue(2, "ar_teller"), "teller is niet null");
            assertNull(zak_recht.getValue(2, "ar_noemer"), "noemer is niet null");
        });
    }
}
