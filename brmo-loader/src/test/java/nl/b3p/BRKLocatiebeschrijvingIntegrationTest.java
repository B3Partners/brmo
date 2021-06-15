/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
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
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Test voor het correct vullen van {@code lo_loc__omschr} kolom van tabel
 * {@code kad_onrrnd_zk} in het RSGB schema. Draaien met:
 * {@code mvn -Dit.test=BRKLocatiebeschrijvingIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle of
 * {@code mvn -Dit.test=BRKLocatiebeschrijvingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Ppostgresql > /tmp/postgresql.log}
 * voor PostgreSQL.
 *
 * @author mprins
 */
@Tag("skip-windows-java11")
public class BRKLocatiebeschrijvingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BRKLocatiebeschrijvingIntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"filename", aantalBerichten, "lo_loc__omschr veld inhoud"},
                arguments("/GH-288/bagadresplusvieradressen.xml", 1, "BRINKSTR 73 A, 9401HZ ASSEN  (3 meer adressen)"),
                arguments("/GH-288/vieradressen.xml", 1, "BRINKSTR 73 A, 9401HZ ASSEN  (3 meer adressen)"),
                arguments("/GH-288/bagadres.xml", 1, null),
                arguments("/GH-288/imkadadres.xml", 1, "WILHELMINASTR 17, 9401NM ASSEN")
        );
    }
    private BrmoFramework brmo;
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private final Lock sequential = new ReentrantLock(true);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

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
        sequential.lock();
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        CleanUtil.cleanRSGB_BAG(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    @ParameterizedTest(name = "Locatie beschrijving #{index}: bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testLocatieBeschrijving(String bestandNaam, long aantalBerichten, String lo_loc__omschr) throws Exception {
        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(BRKLocatiebeschrijvingIntegrationTest.class.getResource(bestandNaam).toURI())));

        if(this.isMsSQL){
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        assumeTrue(aantalBerichten == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Het aantal STAGING_OK berichten is anders dan verwacht");

        List<Bericht> berichten = brmo.listBerichten();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(aantalBerichten, kad_onrrnd_zk.getRowCount(),
                "Het aantal onroerend zaak records komt niet overeen");
        assertEquals(lo_loc__omschr, kad_onrrnd_zk.getValue(0, "lo_loc__omschr"),
                "Beschrijving in record komt niet overeen");
    }
}
