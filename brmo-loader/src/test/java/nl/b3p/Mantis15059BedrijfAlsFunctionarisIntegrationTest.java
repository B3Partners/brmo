/*
 * Copyright (C) 2020 B3Partners B.V.
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integratie test om een nhr dataservice soap bericht te laden en te
 * transformeren, bevat een testcase voor mantis15059; een bedrijf dat een ander berijf als functionaris heeft.
 * <br>Draaien met:
 * {@code mvn -Dit.test=Mantis15059BedrijfAlsFunctionarisIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl :brmo-loader > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=Mantis15059BedrijfAlsFunctionarisIntegrationTest -Dtest.onlyITs=true verify -Pmssql  -pl :brmo-loader > /tmp/mssql.log}
 * voor bijvoorbeeld MS SQL.
 *
 * @author Mark Prins
 */
public class Mantis15059BedrijfAlsFunctionarisIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis15059BedrijfAlsFunctionarisIntegrationTest.class);
    private static final String BESTANDTYPE = "nhr";
    private final Lock sequential = new ReentrantLock(true);
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;


    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"filename", aantalBerichten, aantalProcessen,
                arguments("/mantis15059-nhr-bedrijf-als-fuctionaris/2020-05-28-104333-77513010.anon.xml", 3, 1,
                        // aantalPrs, aantalSubj, aantalNiet_nat_prs, aantalNat_prs,
                        4, 4 + 1, 4, 0,
                        // hoofd vestgID, aantalVestg, aantalVestg_activiteit, kvkNummer v MaatschAct, sbiCodes,
                        "nhr.comVestg.000045210608", 1, 2, 77513010, new String[]{"2825", "7112"},
                        // aantalFunctionarissen},
                        2)
        );
    }

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
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(Mantis15059BedrijfAlsFunctionarisIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue(0L == brmo.getCountBerichten(null, null, BESTANDTYPE, "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, BESTANDTYPE, "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        CleanUtil.cleanRSGB_NHR(rsgb);
        rsgb.close();

        sequential.unlock();
    }

    @ParameterizedTest(name = "{index}: bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testNhrXMLToStagingToRsgb(String bestandNaam, long aantalBerichten, long aantalProcessen,
                                          long aantalPrs, long aantalSubj, long aantalNiet_nat_prs, long aantalNat_prs,
                                          String vestgID, long aantalVestg, long aantalVestg_activiteit,
                                          long kvkNummer, String[] sbiCodes, int aantalFunctionarissen) throws Exception {
        assumeFalse(null == Mantis15059BedrijfAlsFunctionarisIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(BESTANDTYPE, Mantis15059BedrijfAlsFunctionarisIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.info("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");

        // alleen het eerste bericht heeft br_orgineel_xml, de rest niet
        ITable bericht = staging.createQueryTable("bericht", "select * from bericht where volgordenummer=0");
        assertEquals(1, bericht.getRowCount(), "Er zijn meer of minder dan 1 rij");
        LOG.debug("\n\n" + bericht.getValue(0, "br_orgineel_xml") + "\n\n");
        assertNotNull(bericht.getValue(0, "br_orgineel_xml"), "BR origineel xml is null");
        Object berichtId = bericht.getValue(0, "id");

        LOG.info("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        // na de verwerking moet soap payload er ook nog zijn
        bericht = staging.createQueryTable("bericht", "select * from bericht where br_orgineel_xml is not null");
        assertEquals(1, bericht.getRowCount(), "Er zijn meer of minder dan 1 rij");
        assertNotNull(bericht.getValue(0, "br_orgineel_xml"), "BR origineel xml is null na transformatie");
        assertEquals(berichtId, bericht.getValue(0, "id"),
                "bericht met br_orgineel_xml moet hetzelfde id hebben na transformatie");

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, BESTANDTYPE, "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        ITable prs = rsgb.createDataSet().getTable("prs");
        ITable niet_nat_prs = rsgb.createDataSet().getTable("niet_nat_prs");
        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        ITable subject = rsgb.createDataSet().getTable("subject");

        assertEquals(aantalPrs, prs.getRowCount(), "Het aantal 'prs' records klopt niet");
        assertEquals(aantalNiet_nat_prs, niet_nat_prs.getRowCount(),
                "Het aantal 'niet_nat_prs' records klopt niet");
        assertEquals(aantalNat_prs, nat_prs.getRowCount(), "Het aantal 'nat_prs' records klopt niet");
        assertEquals(aantalSubj, subject.getRowCount(), "het aantal 'subject' records klopt niet");

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

        if (vestgID != null) {
            ITable vestg = rsgb.createDataSet().getTable("vestg");
            assertEquals(vestgID, vestg.getValue(0, "sc_identif"),
                    "De 'sc_identif' van hoofdvestiging klopt niet");
            assertEquals(sbiCodes[0], vestg.getValue(0, "fk_sa_sbi_activiteit_sbi_code"),
                    "De sbi code van (hoofd)vestiging klopt niet");
            assertEquals("Ja", vestg.getValue(0, "sa_indic_hoofdactiviteit"),
                    "De 'sa_indic_hoofdactiviteit' van (hoofd)vestiging klopt niet");
        }

        ITable vestg = rsgb.createDataSet().getTable("vestg");
        assertEquals(aantalVestg, vestg.getRowCount(), "het aantal 'vestg' records klopt niet");

        ITable vestg_activiteit = rsgb.createDataSet().getTable("vestg_activiteit");
        assertEquals(aantalVestg_activiteit, vestg_activiteit.getRowCount(),
                "Het aantal 'vestg_activiteit' records klopt niet");

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
        assertEquals(aantalFunctionarissen, functionaris.getRowCount(),
                "Het aantal 'functionaris' records klopt niet");
        if (kvkNummer == 77513010) {
            // gebruik een gesorteerde weergaven van functionaris tabel omdat de volgorde van de rijen anders kan zijn terwijl je dan toch dezelfde data hebt geladen
            // er is een Gevolmachtigde Directeur met beperkte volmacht in de laatste rij
            functionaris = rsgb.createQueryTable("functionaris", "select * from functionaris order by functie ASC");
            assertEquals("Vennoot", functionaris.getValue(aantalFunctionarissen - 1, "functie"));
            assertNull(functionaris.getValue(aantalFunctionarissen - 1, "functionaristypering"));
            assertNull(functionaris.getValue(aantalFunctionarissen - 1, "volledig_beperkt_volmacht"));
            assertEquals("10000", functionaris.getValue(aantalFunctionarissen - 1, "beperking_bev_in_euros").toString());
        }
    }
}
