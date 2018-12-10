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
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Integratie test om een nhr dataservice sopa bericht te laden en te
 * transformeren, bevat een testcase voor mantis10752.
 * <br>Draaien met:
 * {@code mvn -Dit.test=NhrToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=NhrToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}
 * voor bijvoorbeeld MS SQL.
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
public class NhrToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NhrToStagingToRsgbIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"filename", aantalBerichten, aantalProcessen, aantalPrs, aantalSubj, aantalNiet_nat_prs, aantalNat_prs, hoofd vestgID, aantalVestg_activiteit,aantal Subj, kvkNummer v MaatschAct},
            {"/mantis10752/52019667.xml", 2, 1, 1,/*subj*/ 2, 1, 0, "nhr.comVestg.000021991235", 1, 52019667, new String[]{"86912"}, 0},
            /*Verhoef ortho*/
            {"/nhr-v3/52019667.anon.xml", 2, 1, 1,/*subj*/ 2, 1, 0, "nhr.comVestg.000021991235", 1, 52019667, new String[]{"86912"}, 0},
            /*dactari*/
            {"/nhr-v3/16029104.anon.xml", 3, 1, 2 + 6, /*subj*/ 3 + 6, 2, 0 + 6, "nhr.comVestg.000002706229", 1, 16029104, new String[]{"7500"}, 6},
            /*b3p*/
            {"/nhr-v3/34122633,32076598.anon.xml", 3, 1, 2 + 1, /*subj*/ 3 + 1, 2 + 1, 0, "nhr.comVestg.000019315708", 1, 34122633, new String[]{"6202"}, 1},
            /*vereniging ukc*/
            {"/nhr-v3/40480283.anon.xml", 2, 1, 2 + 4, /*subj*/ 2 + 4, 2, 0 + 4, null, 0, 40480283, new String[]{"93152"}, 4},
            /*stichting utr landsch*/
            {"/nhr-v3/41177576.anon.xml", 2, 1, 2 + 12, /*subj*/ 2 + 12, 2, 0 + 12, null, 0, 41177576, new String[]{"91042", "0161", "0150"}, 12},
            /*stichting geo*/
            {"/nhr-v3/32122905.anon.xml", 2, 1, 2 + 4, /*subj*/ 2 + 4, 2, 0 + 4, null, 0, 32122905, new String[]{"7112"}, 4},
            /* min EZ. (nietCommercieleVestiging) */
            {"/nhr-v3/52813150.anon.xml", 3, 1, 2, /*subj*/ 3, 2, 0, "nhr.nietComVestg.000022724362", 1, 52813150, new String[]{"8411"}, 0},
            /*sbb*/
            {"/nhr-v3/30263544.anon.xml", 3, 1, 2, /*subj*/ 3, 2, 0, "nhr.comVestg.000012461547", 1, 30263544, new String[]{"91042"}, 0},
            // apart geval 1 functionaris heef 2 rollen: issue #521
            // het lijkt dat ook niet alle vestigingen worden geladen, alleen de leidende Vestiging #522
            {"/nhr-v3/33257455,23052007.anon.xml", 3, 1, 12, /*subj*/ 13, 3, 9, "nhr.comVestg.000019483104", 5, 33257455, new String[]{"8010", "4321", "4652", "85592", "6202"}, 10}
        });
    }

    private static final String BESTANDTYPE = "nhr";

    /**
     * test parameter.
     */
    private final String bestandNaam;

    /**
     * test parameter.
     */
    private final long aantalBerichten;
    /**
     * test parameter.
     */
    private final long aantalProcessen;
    /**
     * test parameter.
     */
    private final long aantalPrs;
    /**
     * test parameter.
     */
    private final long aantalSubj;
    /**
     * test parameter.
     */
    private final long aantalNiet_nat_prs;
    /**
     * test parameter.
     */
    private final long aantalNat_prs;
    /**
     * test param
     */
    private final String vestgID;
    /**
     * test parameter.
     */
    private final long aantalVestg_activiteit;
    /**
     * test parameter.
     */
    private final long kvkNummer;
    /**
     * test parameter.
     */
    private final String[] sbiCodes;
    /**
     * test parameter.
     */
    private final int aantalFunctionarissen;

    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    public NhrToStagingToRsgbIntegrationTest(String bestandNaam, long aantalBerichten, long aantalProcessen,
            long aantalPrs, long aantalSubj, long aantalNiet_nat_prs, long aantalNat_prs, String vestgID,
            long aantalVestg_activiteit, long kvkNummer, String[] sbiCodes, int aantalFunctionarissen) {
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
        this.aantalPrs = aantalPrs;
        this.aantalSubj = aantalSubj;
        this.aantalNiet_nat_prs = aantalNiet_nat_prs;
        this.aantalNat_prs = aantalNat_prs;
        this.vestgID = vestgID;
        this.aantalVestg_activiteit = aantalVestg_activiteit;
        this.kvkNummer = kvkNummer;
        this.sbiCodes = sbiCodes;
        this.aantalFunctionarissen = aantalFunctionarissen;
    }

    @Before
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
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(NhrToStagingToRsgbIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "nhr", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "nhr", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        CleanUtil.cleanRSGB_NHR(rsgb);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void testNhrXMLToStagingToRsgb() throws Exception {
        assumeNotNull("Het test bestand moet er zijn.", NhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile(BESTANDTYPE, NhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile(), 1L);
        LOG.info("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", aantalProcessen, processen.size());

        // alleen het eerste bericht heeft br_orgineel_xml, de rest niet
        ITable bericht = staging.createQueryTable("bericht", "select * from bericht where volgordenummer=0");
        assertEquals("Er zijn meer of minder dan 1 rij", 1, bericht.getRowCount());
        LOG.debug("\n\n" + bericht.getValue(0, "br_orgineel_xml") + "\n\n");
        assertNotNull("BR origineel xml is null", bericht.getValue(0, "br_orgineel_xml"));
        Object berichtId = bericht.getValue(0, "id");

        LOG.info("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        // na de verwerking moet soap payload er ook nog zijn
        bericht = staging.createQueryTable("bericht", "select * from bericht where br_orgineel_xml is not null");
        assertEquals("Er zijn meer of minder dan 1 rij", 1, bericht.getRowCount());
        assertNotNull("BR origineel xml is null na transformatie", bericht.getValue(0, "br_orgineel_xml"));
        assertEquals("bericht met br_orgineel_xml moet hetzelfde id hebben na transformatie", berichtId, bericht.getValue(0, "id"));

        assertEquals("Niet alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "nhr", "RSGB_OK"));
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
        }

        ITable prs = rsgb.createDataSet().getTable("prs");
        ITable niet_nat_prs = rsgb.createDataSet().getTable("niet_nat_prs");
        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        ITable subject = rsgb.createDataSet().getTable("subject");

        assertEquals("Het aantal 'prs' records klopt niet", aantalPrs, prs.getRowCount());
        assertEquals("Het aantal 'niet_nat_prs' records klopt niet", aantalNiet_nat_prs, niet_nat_prs.getRowCount());
        assertEquals("Het aantal 'nat_prs' records klopt niet", aantalNat_prs, nat_prs.getRowCount());
        assertEquals("het aantal 'subject' records klopt niet", aantalSubj, subject.getRowCount());

        boolean foundKvk = false;
        for (int i = 0; i < subject.getRowCount(); i++) {
            if (subject.getValue(i, "identif").toString().contains("nhr.maatschAct.kvk")) {
                assertEquals("KVK nummer klopt niet", kvkNummer + "", subject.getValue(i, "kvk_nummer") + "");
                foundKvk = true;
            }
        }
        if (!foundKvk) {
            fail("KVK nummer maatschappelijke activiteit klopt niet, verwacht te vinden: " + kvkNummer);
        }

        if (vestgID != null) {
            ITable vestg = rsgb.createDataSet().getTable("vestg");
            assertEquals("De 'sc_identif' van hoofdvestiging klopt niet", vestgID, vestg.getValue(0, "sc_identif"));
            assertEquals("De sbi code van (hoofd)vestiging klopt niet", sbiCodes[0], vestg.getValue(0, "fk_sa_sbi_activiteit_sbi_code"));
            assertEquals("De 'sa_indic_hoofdactiviteit' van (hoofd)vestiging klopt niet", "Ja", vestg.getValue(0, "sa_indic_hoofdactiviteit"));
        }
        // TODO check aantal vestigingen, zie ook: #522, nu hooguit 1 hoofdVestg...
        // assertEquals("het aantal 'vestg' records klopt niet", aantalVestg, vestg.getRowCount());

        ITable vestg_activiteit = rsgb.createDataSet().getTable("vestg_activiteit");
        assertEquals("Het aantal 'vestg_activiteit' records klopt niet", aantalVestg_activiteit, vestg_activiteit.getRowCount());

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

        ITable functionaris = rsgb.createDataSet().getTable("functionaris");
        assertEquals("Het aantal 'functionaris' records klopt niet", aantalFunctionarissen, functionaris.getRowCount());
        if (kvkNummer == 41177576) {
            // er is een Gevolmachtigde Directeur met beperkte volmacht in rij 2
            // dit is fout gevoelig omdat de volgorde van de rijen anders kan zijn terwijl je dan toch dezelfde data hebt geladen
            assertEquals("Gevolmachtigde", functionaris.getValue(1, "functie"));
            assertEquals("Directeur", functionaris.getValue(1, "functionaristypering"));
            assertEquals("B", functionaris.getValue(1, "volledig_beperkt_volmacht"));
        }
    }
}
