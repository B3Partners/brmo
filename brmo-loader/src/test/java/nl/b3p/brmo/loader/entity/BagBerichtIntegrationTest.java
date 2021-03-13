/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.entity;

import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
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
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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

import static nl.b3p.brmo.loader.BrmoFramework.BR_BAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Draaien met:
 * {@code mvn -Dit.test=BagBerichtIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Ppostgresql >
 * /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
 *
 * @author mprins
 */
@Tag("skip-windows-java11")
public class BagBerichtIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BagBerichtIntegrationTest.class);

    private final Lock sequential = new ReentrantLock();
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private BrmoFramework brmo;

    /**
     * testdata.
     *
     * @return een test data {@code Object[]} met daarin
     * {@code {"bestandNaam", aantalBerichten, aantalLaadProcessen, "timestamp"}}
     * voor bag berichten
     */
    public static Collection<Object[]> testdata() {
        return Arrays.asList(new Object[][]{
                // {"bestandNaam", aantalBerichten, aantalLaadProcessen, timestamp},
                {"/GH-280/b1.xml", 1, 1, "2016-03-08T16:00:09.000023"},
                {"/GH-280/b2.xml", 1, 1, "2016-03-08T16:00:12.000199"}
        });
    }

    @BeforeEach
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
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()),
                    params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        // lege bericht en laadproces tabellen
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(
                new File(BagBerichtIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        assumeTrue(0L == brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"),
                "Er zijn STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"),
                "Er zijn STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
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
        Iterator<Object[]> i = testdata().iterator();
        Object[] data = i.next();
        String bestandNaam = (String) data[0];
        int aantalBerichten = (int) data[1];
        int aantalProcessen = (int) data[2];
        String timestamp = (String) data[3];

        brmo.loadFromFile(BR_BAG, BagBerichtIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"),
                "Aantal berichten is niet als verwacht");
        assertEquals(aantalProcessen, brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"),
                "Aantal laadprocessen is niet als verwacht");

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals(
                LocalDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")),
                bericht.getValue(0, "datum").toString(),
                "Datum komt niet overeen");
    }

    /**
     * laad de berichten in de staging db, verwerk vervolgens naar rsgb.
     *
     * @throws Exception if any
     */
    @Test
    public void loadAll() throws Exception {
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
            brmo.loadFromFile(BR_BAG, BagBerichtIntegrationTest.class.getResource(data[0].toString()).getFile(), null);
            _aantalBerichten += (int) data[1];
            _aantalProcessen += (int) data[2];
        }

        assertEquals(_aantalBerichten, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"),
                "Aantal berichten is niet als verwacht");
        assertEquals(_aantalProcessen, brmo.getCountLaadProcessen(null, null, BR_BAG, "STAGING_OK"),
                "Aantal laadprocessen is niet als verwacht");

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals(2, bericht.getRowCount(), "Het aantal bericht");
        Date datum_b1 = (Date) bericht.getValue(0, "datum");
        Date datum_b2 = (Date) bericht.getValue(1, "datum");

        Thread t = brmo.toRsgb();
        t.join();

        ITable pand = rsgb.createDataSet().getTable("pand");
        assertEquals(1, pand.getRowCount(), "Het aantal panden klopt niet");
        assertEquals("0613100000136918", pand.getValue(0, "identif"), "Pand identificatie klopt niet");

        ITable pand_archief = rsgb.createDataSet().getTable("pand_archief");
        assertEquals(1, pand_archief.getRowCount(), "Het aantal archief panden");
        assertEquals(
                pand_archief.getValue(0, "datum_einde_geldh"), pand_archief.getValue(0, "dat_beg_geldh"),
                "Datums komen niet overeen");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(1, brondocument.getRowCount(), "Het aantal brondocumenten klopt niet");
        assertEquals("VA1071182", brondocument.getValue(0, "identificatie"), "brondocumentnummer klopt niet");

        assertEquals(0, brmo.getCountBerichten(null, null, BR_BAG, "STAGING_OK"),
                "Aantal verwerkte berichten is niet als verwacht");

        ITable bericht2 = staging.createDataSet().getTable("bericht");
        assertEquals(datum_b1, bericht2.getValue(0, "datum"),
                "Datum voorafgaan en na afloop van transformatie komt niet overeen");
        assertEquals(datum_b2, bericht2.getValue(1, "datum"),
                "Datum voorafgaan en na afloop van transformatie komt niet overeen");
    }
}
