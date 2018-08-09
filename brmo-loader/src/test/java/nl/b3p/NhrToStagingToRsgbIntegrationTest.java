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
            // {"type","filename", aantalBerichten, aantalProcessen, aantalPrs, aantalNiet_nat_prs, aantalNat_prs, vestgID, aantalVestg_activiteit},
            {"/mantis10752/52019667.xml", 2, 1, 1,/*subj*/ 2, 1, 0, "nhr.comVestg.000021991235", 1, 52019667},
            /*Test automatisering*/
            {"/nl/b3p/brmo/loader/xml/nhr-2.5_0.xml", 3, 1, 2, 3, 2, 0, "nhr.comVestg.000016444663", 3, 12345678},
            /*Verhoef ortho*/
            {"/nhr-v2.5/52019667.anon.xml", 2, 1, 1,/*subj*/ 2, 1, 0, "nhr.comVestg.000021991235", 1, 52019667},
            /*dactari*/
            {"/nhr-v2.5/16029104.anon.xml", 3, 1, 2, /*subj*/ 3, 2, 0, "nhr.comVestg.000002706229", 1, 16029104},
            /*b3p*/
            {"/nhr-v2.5/34122633,32076598.anon.xml", 3, 1, 2, /*subj*/ 3, 2, 0, "nhr.comVestg.000019315708", 1, 34122633},
            /*ukc*/
            {"/nhr-v2.5/40480283.anon.xml", 2, 1, 2, /*subj*/ 2, 2, 0, null, 0, 40480283},});
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

    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    public NhrToStagingToRsgbIntegrationTest(String bestandNaam, long aantalBerichten, long aantalProcessen,
            long aantalPrs, long aantalSubj, long aantalNiet_nat_prs, long aantalNat_prs, String vestgID,
            long aantalVestg_activiteit, long kvkNummer) {
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

        brmo.loadFromFile(BESTANDTYPE, NhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile());
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", aantalProcessen, processen.size());

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

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

        // faalt voor vereniging 40480283
        if (kvkNummer != 40480283) {
            boolean foundKvk = false;
            for (int i = 0; i < subject.getRowCount(); i++) {
                if (subject.getValue(i, "identif").toString().contains("nhr.maatschAct.kvk")) {
                    assertEquals("KVK nummer klopt niet", kvkNummer + "", subject.getValue(i, "kvk_nummer") + "");
                    foundKvk = true;
                }
            }
            if (!foundKvk) {
                fail("KVK nummer klopt niet, verwacht te vinden: " + kvkNummer);
            }
        }

        if (vestgID != null) {
            ITable vestg = rsgb.createDataSet().getTable("vestg");
            assertEquals("De 'sc_identif' van vestiging klopt niet", vestgID, vestg.getValue(0, "sc_identif"));
        }
        ITable vestg_activiteit = rsgb.createDataSet().getTable("vestg_activiteit");
        assertEquals("Het aantal 'vestg_activiteit' records klopt niet", aantalVestg_activiteit, vestg_activiteit.getRowCount());
    }
}
