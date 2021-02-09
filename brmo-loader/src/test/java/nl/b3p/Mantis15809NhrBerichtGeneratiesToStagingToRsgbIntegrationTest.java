/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Integratie test om een nhr dataservice soap bericht te laden en te transformeren,
 * bevat een testcase voor mantis15809 waarbij berichten van dezelfde inschrijving op verschillende tijdstippen
 * worden geladen.
 * <br>Draaien met:
 * {@code mvn -Dit.test=Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl :brmo-loader > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Pmssql  -pl :brmo-loader > /tmp/mssql.log}
 * voor bijvoorbeeld MS SQL of
 * {@code mvn -Dit.test=Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Poracle  -pl :brmo-loader > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author Mark Prins
 */

public class Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(
            Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest.class);

    private static final String BESTANDTYPE = "nhr";
    private final Lock sequential = new ReentrantLock(true);
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);
        dsStaging.setConnectionProperties(params.getProperty("staging.options", ""));

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
        dsRsgb.setConnectionProperties(params.getProperty("rsgb.options", ""));

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()),
                    params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        sequential.lock();

        // staging en rsgb tabellen leeg maken voorafgaan aan de test
        CleanUtil.cleanSTAGING(staging, false);
        CleanUtil.cleanRSGB_NHR(rsgb);

        Assumptions.assumeTrue(0L == brmo.getCountBerichten(null, null, "nhr", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        Assumptions.assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "nhr", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    /**
     * staging en rsgb tabellen leeg maken
     *
     * @throws Exception is any
     */
    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        CleanUtil.cleanRSGB_NHR(rsgb);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void test() throws Exception {
        // te laden berichten + kvknummer + vestg nummer + sbicode + aantallen
        Object[][] testdataSet = new Object[][]{
                // {"filename", aantalBerichten, aantalProcessen, hoofdVestgID, aantalVestg_activiteit,
                // kvkNummer, sbiCodes (eerste  sbiCode is hoofdactiviteit) },
                {"/mantis15809-nhr-bericht-generaties/78597161_20200903.anon.xml", 3, 1, "nhr.comVestg.000006873758", 4,
                        78597161, new String[]{"46738", "47789", "47918", "4329"}},
                {"/mantis15809-nhr-bericht-generaties/78597161_20201029.anon.xml", 3, 1, "nhr.comVestg.000006873758", 4,
                        78597161, new String[]{"46738", "47789", "47918", "4329"}},
                {"/mantis15809-nhr-bericht-generaties/78597161_20210127.anon.xml", 3, 1, "nhr.comVestg.000006873758", 2,
                        78597161, new String[]{"4329", "4339"}}
        };

        String bestandNaam;
        long aantalBerichten = 0;
        long aantalProcessen = 0;
        String vestgID;
        long aantalVestg_activiteit;
        long kvkNummer;
        String[] sbiCodes = new String[]{};
        int verwerktNHr = 0;

        // berichten laden en transformeren
        for (Object[] testdata : testdataSet) {
            bestandNaam = (String) testdata[0];
            aantalBerichten += Long.valueOf((Integer) testdata[1]);
            aantalProcessen += Long.valueOf((Integer) testdata[2]);
            vestgID = (String) testdata[3];
            aantalVestg_activiteit = Long.valueOf((Integer) testdata[4]);
            kvkNummer = Long.valueOf((Integer) testdata[5]);
            sbiCodes = (String[]) testdata[6];

            testNhrXMLToStagingToRsgb(bestandNaam, aantalBerichten, aantalProcessen,
                    vestgID, aantalVestg_activiteit, kvkNummer, sbiCodes, ++verwerktNHr);
        }

        // dubbele controle van aantal brmo berichten/laadprocessen als alles geladen/getransformeerd is
        assertEquals(aantalBerichten, brmo.listBerichten().size(), "Het aantal berichten is niet als verwacht.");
        assertEquals(aantalProcessen, brmo.listLaadProcessen().size(), "Het aantal processen is niet als verwacht.");

        // dubbele controle van vestg_activiteit als alles geladen/getransformeerd is
        ITable vestg_activiteit = rsgb.createDataSet().getTable("vestg_activiteit");
        assertEquals(sbiCodes.length, vestg_activiteit.getRowCount(),
                "Het aantal 'vestg_activiteit' records klopt niet");
        vestg_activiteit = rsgb.createQueryTable("vestg_activiteit",
                "select * from vestg_activiteit where indicatie_hoofdactiviteit=1");
        assertEquals(sbiCodes[0], vestg_activiteit.getValue(0, "fk_sbi_activiteit_code"),
                "Hoofdactiviteit in 'vestg_activiteit' klopt niet");
        assertEquals(1, vestg_activiteit.getRowCount(),
                "Aantal hoofdactiviteiten in 'vestg_activiteit' klopt niet");
    }

    private void testNhrXMLToStagingToRsgb(String bestandNaam, long aantalBerichten, long aantalProcessen,
                                           String vestgID, long aantalVestg_activiteit, long kvkNummer,
                                           String[] sbiCodes, int verwerktNHr) throws Exception {

        assumeFalse(null == Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest.class.getResource(
                bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(BESTANDTYPE, Mantis15809NhrBerichtGeneratiesToStagingToRsgbIntegrationTest.class.getResource(
                bestandNaam).getFile(), null);
        LOG.info("klaar met laden van berichten in staging DB.");

        // check aantallen brmo berichten
        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");

        // alleen het eerste bericht van een nHR bericht heeft br_orgineel_xml, de rest niet, check dat
        ITable bericht = staging.createQueryTable("bericht", "select * from bericht where volgordenummer=0 order by id");
        assertEquals(verwerktNHr, bericht.getRowCount(), "Er zijn meer of minder dan 1 rij");
        LOG.debug("\n\n" + bericht.getValue(verwerktNHr - 1, "br_orgineel_xml") + "\n\n");
        assertNotNull(bericht.getValue(verwerktNHr - 1, "br_orgineel_xml"), "BR origineel xml is null");
        Object berichtId = bericht.getValue(verwerktNHr - 1, "id");

        LOG.info("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        // na de verwerking moet soap payload er ook nog zijn
        bericht = staging.createQueryTable("bericht", "select * from bericht where br_orgineel_xml is not null order by id");
        assertEquals(verwerktNHr, bericht.getRowCount(), "Er zijn meer of minder dan verwacht rijen");
        assertNotNull(bericht.getValue(verwerktNHr - 1, "br_orgineel_xml"),
                "BR origineel xml is null na transformatie");
        assertEquals(berichtId, bericht.getValue(verwerktNHr - 1, "id"),
                "bericht met br_orgineel_xml moet hetzelfde id hebben na transformatie");

        // controle van transformatie
        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "nhr", "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        // check kvk nummer
        ITable subject = rsgb.createDataSet().getTable("subject");
        boolean foundKvk = false;
        for (int i = 0; i < subject.getRowCount(); i++) {
            if (subject.getValue(i, "identif").toString().contains("nhr.maatschAct.kvk")) {
                assertEquals(kvkNummer + "", subject.getValue(i, "kvk_nummer") + "", "KVK nummer klopt niet");
                foundKvk = true;
            }
        }
        if (!foundKvk) {
            fail("KVK nummer maatschappelijke activiteit klopt niet, verwacht te vinden: " + kvkNummer);
        }

        // check vestiging en activiteit
        ITable vestg = rsgb.createDataSet().getTable("vestg");
        assertEquals(vestgID, vestg.getValue(0, "sc_identif"),
                "De 'sc_identif' van hoofdvestiging klopt niet");
        assertEquals(sbiCodes[0], vestg.getValue(0, "fk_sa_sbi_activiteit_sbi_code"),
                "De sbi code van (hoofd)vestiging klopt niet");
        assertEquals("Ja", vestg.getValue(0, "sa_indic_hoofdactiviteit"),
                "De 'sa_indic_hoofdactiviteit' van (hoofd)vestiging klopt niet");

        // check vestingingsactiviteiten
        ITable vestg_activiteit = rsgb.createDataSet().getTable("vestg_activiteit");
        assertEquals(aantalVestg_activiteit, vestg_activiteit.getRowCount(),
                "Het aantal 'vestg_activiteit' records klopt niet");

        vestg_activiteit = rsgb.createQueryTable("vestg_activiteit",
                "select * from vestg_activiteit where indicatie_hoofdactiviteit=1");
        assertEquals(sbiCodes[0], vestg_activiteit.getValue(0, "fk_sbi_activiteit_code"),
                "Hoofdactiviteit in 'vestg_activiteit' klopt niet");
        assertEquals(1, vestg_activiteit.getRowCount(),
                "Aantal hoofdactiviteit in 'vestg_activiteit' klopt niet");

        ITable sbi_activiteit = rsgb.createDataSet().getTable("sbi_activiteit");
        ITable herkomst_metadata = rsgb.createDataSet().getTable("herkomst_metadata");
        for (String sbiCode : sbiCodes) {
            boolean foundSbiCode = false;
            boolean foundSbiCodeMeta = false;
            for (int i = 0; i < sbi_activiteit.getRowCount(); i++) {
                if (sbiCode.equals(sbi_activiteit.getValue(i, "sbi_code").toString())) {
                    LOG.debug("SBI code " + sbiCode + " gevonden in sbi_activiteit");
                    foundSbiCode = true;
                    break;
                }
            }
            if (!foundSbiCode) {
                fail("SBI code niet gevonden in sbi_activiteit, verwacht te vinden: " + sbiCode);
            }
            for (int i = 0; i < herkomst_metadata.getRowCount(); i++) {
                if (sbiCode.equals(herkomst_metadata.getValue(i, "waarde").toString())) {
                    LOG.debug("SBI code " + sbiCode + " gevonden in herkomst_metadata");
                    foundSbiCodeMeta = true;
                    break;
                }
            }
            if (!foundSbiCodeMeta) {
                fail("SBI code niet gevonden in herkomst_metadata, verwacht te vinden: " + sbiCode);
            }
        }

    }
}
