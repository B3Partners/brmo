/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.entity;

import nl.b3p.AbstractDatabaseIntegrationTest;
import static nl.b3p.brmo.loader.BrmoFramework.BR_BAG;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=BagBerichtIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BagBerichtIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BagBerichtIntegrationTest.class);

    private static boolean runloadAllTestAgain = true;

    /**
     * testdata.
     *
     * @return een test data {@code Object[]} met daarin
     * {@code {"bestandNaam", aantalBerichten, aantalLaadProcessen, "timestamp"}}
     * boor bag berichten
     */
    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection testdata() {
        return Arrays.asList(new Object[][]{
            // {"bestandNaam", aantalBerichten, aantalLaadProcessen, timestamp},
            {"/GH-280/b1.xml", 1, 1, "2016-03-08T16:00:09.000023"},
            {"/GH-280/b2.xml", 1, 1, "2016-03-08T16:00:12.000199"}
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
    private final long aantalProcessen;
    /**
     * test parameter.
     */
    private final String timestamp;

    private final Lock sequential = new ReentrantLock();
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private BrmoFramework brmo;

    public BagBerichtIntegrationTest(String bestandNaam, long aantalBerichten, long aantalProcessen, String timestamp) {
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
        this.timestamp = timestamp;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);
        staging = new DatabaseDataSourceConnection(dsStaging);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        brmo = new BrmoFramework(dsStaging, dsRsgb, null);
        brmo.setOrderBerichten(true);

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
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        // lege bericht en laadproces tabellen
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BagBerichtTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        assumeTrue("Er zijn STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"));
        assumeTrue("Er zijn STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        CleanUtil.cleanRSGB_BAG(rsgb, true);
        rsgb.close();

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @Test
    public void loadInStaging() throws Exception {
        brmo.loadFromFile(BR_BAG, BagBerichtTest.class.getResource(bestandNaam).getFile());
        assertEquals("Aantal berichten is niet als verwacht", aantalBerichten, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"));
        assertEquals("Aantal laadprocessen is niet als verwacht", aantalProcessen, brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"));

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Datum komt niet overeen",
                LocalDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")),
                bericht.getValue(0, "datum").toString()
        );
    }

    /**
     * laadt de berichten in de staging db, verwerk vervolgens naar rsgb.
     *
     * @throws Exception if any
     */
    @Test
    public void loadAll() throws Exception {
        // deze test maar 1x draaien, vanwege testdata parametrisatie wordt de test voor iedere testdata gestart
        // met een assumption staat deze case 1x skipped in de lijst
        assumeTrue("Test is al een keer gestart.", runloadAllTestAgain);
        // if (runloadAllTestAgain) { return; }
        runloadAllTestAgain = false;

        int _aantalBerichten = 0;
        int _aantalProcessen = 0;

        Iterator<Object[]> i = testdata().iterator();
        LOG.debug("Aantal te laden testdata berichten: " + testdata().size());
        while (i.hasNext()) {
            Object[] data = i.next();
            // LOG.debug("bestandNaam:     " + data[0]);
            // LOG.debug("aantalBerichten: " + data[1]);
            // LOG.debug("aantalProcessen: " + data[2]);
            // LOG.debug("timestamp:       " + data[3]);
            brmo.loadFromFile(BR_BAG, BagBerichtTest.class.getResource(data[0].toString()).getFile());
            _aantalBerichten += (int) data[1];
            _aantalProcessen += (int) data[2];
        }

        assertEquals("Aantal berichten is niet als verwacht", _aantalBerichten, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"));
        assertEquals("Aantal laadprocessen is niet als verwacht", _aantalProcessen, brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"));

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Het aantal bericht", 2, bericht.getRowCount());
        Date datum_b1 = (Date) bericht.getValue(0, "datum");
        Date datum_b2 = (Date) bericht.getValue(1, "datum");

        Thread t = brmo.toRsgb();
        t.join();

        ITable pand = rsgb.createDataSet().getTable("pand");
        assertEquals("Het aantal panden klopt niet", 1, pand.getRowCount());
        assertEquals("Pand identificatie klopt niet", "0613100000136918", pand.getValue(0, "identif"));

        ITable pand_archief = rsgb.createDataSet().getTable("pand_archief");
        assertEquals("Het aantal archief panden", 1, pand_archief.getRowCount());
        assertEquals("Datums komen niet overeen", pand_archief.getValue(0, "datum_einde_geldh"), pand_archief.getValue(0, "dat_beg_geldh"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Het aantal brondocumenten klopt niet", 1, brondocument.getRowCount());
        assertEquals("brondocumentnummer klopt niet", "VA1071182", brondocument.getValue(0, "identificatie"));

        assertEquals("Aantal verwerkte berichten is niet als verwacht", 0, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"));

        ITable bericht2 = staging.createDataSet().getTable("bericht");
        assertEquals("Datum voorafgaan en na afloop van transformatie komt niet overeen", datum_b1, bericht2.getValue(0, "datum"));
        assertEquals("Datum voorafgaan en na afloop van transformatie komt niet overeen", datum_b2, bericht2.getValue(1, "datum"));
    }
}
