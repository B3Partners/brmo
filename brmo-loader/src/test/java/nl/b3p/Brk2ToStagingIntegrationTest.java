/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Draaien met:
 * {@code mvn -Dit.test=Brk2ToStagingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log} of
 * {@code mvn -Dit.test=Brk2ToStagingIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-loader > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author mprins
 */
class Brk2ToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Brk2ToStagingIntegrationTest.class);
    //    private BasicDataSource dsRsgb;
    private final Lock sequential = new ReentrantLock(true);
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    //    private IDatabaseConnection rsgb;
    private BasicDataSource dsStaging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"type","filename", aantalBerichten, aantalProcessen, "datumEersteMutatie"},
                arguments("brk2", "/brk2/MUTKX02-ABG00F1856-20211012-1.anon.xml", "NL.IMKAD.KadastraalObject:5260185670000", 1, 1, "2021-10-12"),
                arguments("brk2", "/brk2/MUTKX02-ABG00F1856-20211102-1.anon.xml", "NL.IMKAD.KadastraalObject:5260185670000", 1, 1, "2021-11-02", 1),
                // stand
                arguments("brk2", "/brk2/GEBKX02-20210610-1.anon.xml", "NL.IMKAD.KadastraalObject:50247970000", 1, 1, "2021-06-10")
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

//        dsRsgb = new BasicDataSource();
//        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
//        dsRsgb.setUsername(params.getProperty("rsgb.user"));
//        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
//        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        staging = new DatabaseDataSourceConnection(dsStaging);
//        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

//            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
//            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
//            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
//            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, null);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(Brk2ToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        Assumptions.assumeTrue(0L == brmo.getCountBerichten(null, null, "brk2", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        Assumptions.assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "brk2", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        dsStaging.close();

//        CleanUtil.cleanRSGB_BRK(rsgb, true);
//        rsgb.close();
//        dsRsgb.close();

        sequential.unlock();
    }

    @DisplayName("BRK2 XML in staging laden")
    @ParameterizedTest(name = "testBrk2XMLToStaging #{index}: type: {0}, bestand: {1}, mutatiedatum: {4}")
    @MethodSource("argumentsProvider")
    void testBrk2XMLToStaging(String bestandType, String bestandNaam, String objectRef, long aantalBerichten, long aantalProcessen, String datumEersteMutatie) throws Exception {
        assumeFalse(null == Brk2ToStagingIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(bestandType, Brk2ToStagingIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertEquals(aantalBerichten, brmo.getCountLaadProcessen(null, null, "brk2", "STAGING_OK"), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");

        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getBrXml(), "'br-xml' van bericht is 'null'");
        }
        assertEquals(objectRef, berichten.get(0).getObjectRef(), "Het bericht heeft niet de juiste objectRef.");

//        LOG.debug("Transformeren berichten naar rsgb DB.");
//        Thread t = brmo.toRsgb();
//        t.join();
//
//        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "brk2", "RSGB_OK"),
//                "Niet alle berichten zijn OK getransformeerd");
//        berichten = brmo.listBerichten();
//        for (Bericht b : berichten) {
//            assertNotNull(b, "Bericht is 'null'");
//            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
//        }
//
//        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
//        assertEquals(aantalBerichten, kad_onrrnd_zk.getRowCount(), "Het aantal kad_onrrnd_zk klopt niet");
//        assertEquals(datumEersteMutatie, kad_onrrnd_zk.getValue(0, "dat_beg_geldh"),
//                "Datum eerste record komt niet overeen");
//
//        if(bestandNaam.contains("BRK_XML.anon.xml")){
//            // check of aand_naamgebruik leeg is voor alle personen
//            ITable nat_prs =rsgb.createDataSet().getTable("nat_prs");
//            assertEquals(2, nat_prs.getRowCount(), "aantal natuurlijke personen klopt niet");
//
//            nat_prs = rsgb.createQueryTable("nat_prs","select * from nat_prs where geslachtsaand = 'M'");
//            assertEquals(1, nat_prs.getRowCount(), "aantal 'M' klopt niet");
//            assertNull(nat_prs.getValue(0,"aand_naamgebruik"));
//
//            // ondanks dat bericht zegt "Eigen naam" wat een ongeldige waarde is voor //GbaPersoon:aanduidingNaamgebruik/Typen:code
//            nat_prs = rsgb.createQueryTable("nat_prs","select * from nat_prs where geslachtsaand = 'V'");
//            assertEquals(1, nat_prs.getRowCount(), "aantal 'V' klopt niet");
//            assertNull(nat_prs.getValue(0,"aand_naamgebruik"));
//        }
    }
}
