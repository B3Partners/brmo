/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
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
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test voor het correct vullen van {@code lo_loc__omschr} kolom van tabel
 * {@code kad_onrrnd_zk} in het RSGB schema. Draaien met:
 * {@code mvn -Dit.test=BRKLocatiebeschrijvingIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle of
 * {@code mvn -Dit.test=BRKLocatiebeschrijvingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor PostgreSQL.
 *
 * <strong>NB. werkt niet op mssql</strong>, althans niet met de jTDS driver
 * omdat die geen JtdsPreparedStatement#setNull() methode heeft.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class BRKLocatiebeschrijvingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BRKLocatiebeschrijvingIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"filename", aantalBerichten, "lo_loc__omschr veld inhoud"},
            {"/GH-288/bagadresplusvieradressen.xml", 1, "BRINKSTR 73 A, 9401HZ ASSEN  (3 meer adressen)"},
            {"/GH-288/vieradressen.xml", 1, "BRINKSTR 73 A, 9401HZ ASSEN  (3 meer adressen)"},
            {"/GH-288/bagadres.xml", 1, null},
            {"/GH-288/imkadadres.xml", 1, "WILHELMINASTR 17, 9401NM ASSEN"}
        });
    }
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
    private final String lo_loc__omschr;

    private BrmoFramework brmo;
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    public BRKLocatiebeschrijvingIntegrationTest(String bestandNaam, long aantalBerichten, String lo_loc__omschr) {
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.lo_loc__omschr = lo_loc__omschr;
    }

    @Before
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

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6166IntegrationTest.class.getResource(bestandNaam).toURI())));

        sequential.lock();
        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Het aantal STAGING_OK berichten is anders dan verwacht", aantalBerichten == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        CleanUtil.cleanRSGB_BAG(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void testLocatieBeschrijving() throws Exception {

        List<Bericht> berichten = brmo.listBerichten();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal onroerend zaak records komt niet overeen", aantalBerichten, kad_onrrnd_zk.getRowCount());
        assertEquals("Beschrijving in record komt niet overeen", lo_loc__omschr, kad_onrrnd_zk.getValue(0, "lo_loc__omschr"));
    }
}
