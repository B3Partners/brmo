/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.PostgreSQLDriverBasedFailures;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Testcases voor mantis-6512 en mantis-6727; Draaien met:
 * {@code mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle, deze test is niet relevent voor andere databases. Om de driver
 * te overriden gebruik je de properties -Doracle.jdbc.artifactId=ojdbc7
 * -Doracle.jdbc.version=12.1.0.2.0 bijvoorbeeld..
 *
 * <pre>
 * {@code
 * mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Doracle.jdbc.artifactId=ojdbc6 -Doracle.jdbc.version=11.2.0.4.0 -Poracle > target/ojdbc6-11.2.0.4.0-oracle.log
 * mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Doracle.jdbc.artifactId=ojdbc6 -Doracle.jdbc.version=12.1.0.2.0 -Poracle > target/ojdbc6-12.1.0.2.0-oracle.log
 * mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Doracle.jdbc.artifactId=ojdbc7 -Doracle.jdbc.version=12.1.0.2.0 -Poracle > target/ojdbc7-12.1.0.2.0-oracle.log
 * mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Doracle.jdbc.artifactId=ojdbc8 -Doracle.jdbc.version=12.2.0.1.0 -Poracle > target/ojdbc8-12.2.0.1.0-oracle.log
 * }
 * </pre>
 * 
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category({JTDSDriverBasedFailures.class, PostgreSQLDriverBasedFailures.class})
public class Oracle8000byteBugIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Oracle8000byteBugIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"type","filename", brxml bytes, aantalBerichten, aantalProcessen, "datumEersteMutatie"},
            {"brk", "/8000byteBericht/8000bytesBRxml.xml", 8001, 1, 1, "2017-08-02 00:00:00.0"},
            {"brk", "/8000byteBericht/8001bytesBRxml.xml", 8001, 1, 1, "2017-08-02 00:00:00.0"},
            {"brk", "/8000byteBericht/7999bytesBRxml.xml", 7999, 1, 1, "2017-08-02 00:00:00.0"},
            {"brk", "/8000byteBericht/PD_MUTBX01_23-8-2017.xml", 8093, 1, 1, "2017-08-02 00:00:00.0"},
            {"brk", "/8000byteBericht/MUTBX01.xml", 8093, 1, 1, "2017-07-03 00:00:00.0"}
        });
    }

    /**
     * test parameter.
     */
    private final String bestandNaam;

    /**
     * test param.
     */
    private final int brxml_bytes;
    /**
     * test parameter.
     */
    private final String bestandType;
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
    private final String datumEersteMutatie;

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();

    public Oracle8000byteBugIntegrationTest(String bestandType, String bestandNaam, int brxml_bytes, long aantalBerichten, long aantalProcessen, String datumEersteMutatie) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
        this.brxml_bytes = brxml_bytes;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
        this.datumEersteMutatie = datumEersteMutatie;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        assumeTrue("Deze test is alleen voor Oracle.", this.isOracle);

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

        staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
        staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

        rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
        rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(Oracle8000byteBugIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            brmo.closeBrmoFramework();
        }

        if (rsgb != null) {
            CleanUtil.cleanRSGB_BRK(rsgb, true);
            rsgb.close();
        }

        if (staging != null) {
            CleanUtil.cleanSTAGING(staging);
            DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
                new DefaultTable("job")}
            ));
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed");
        }
    }

    @Test
    public void testBrkXMLToStaging() throws Exception {
        assumeNotNull("Het test bestand moet er zijn.", Oracle8000byteBugIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile(bestandType, Oracle8000byteBugIntegrationTest.class.getResource(bestandNaam).getFile());
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", aantalProcessen, processen.size());

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Het aantal berichten klopt niet", aantalBerichten, bericht.getRowCount());
        assertEquals("De datum klopt niet", datumEersteMutatie, bericht.getValue(0, "datum").toString());

        assertEquals("incorrect aantal bytes", brxml_bytes, bericht.getValue(0, "br_xml").toString().getBytes().length);
        assertNull("DB xml is niet null (want nog niet getransformeerd)", bericht.getValue(0, "db_xml"));
        assertFalse("BR xml is vernachelt", bericht.getValue(0, "br_xml").toString().contains("? x m l"));
        assertFalse("BR xml is vernachelt", bericht.getValue(0, "br_xml").toString().startsWith(" < ? x m l"));

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
            // test toepassing van de 8000byte HACK in de stagingproxy
            int dflt = b.getDbXml().getBytes().length;
            int utf8 = b.getDbXml().getBytes(Charset.forName("UTF-8")).length;
            assertNotEquals("db xml is 8000 bytes", 8000, dflt);
            assertNotEquals("db xml in UTF8 is 8000 bytes", 8000, utf8);

            dflt = b.getBrXml().getBytes().length;
            utf8 = b.getBrXml().getBytes(Charset.forName("UTF-8")).length;
            assertNotEquals("br xml is 8000 bytes", 8000, dflt);
            assertNotEquals("br xml in UTF8 is 8000 bytes", 8000, utf8);
        }

        bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Het aantal berichten", aantalBerichten, bericht.getRowCount());
        assertEquals("8000 bytes", brxml_bytes, bericht.getValue(0, "br_xml").toString().getBytes().length);
        assertFalse("BR xml is vernachelt", bericht.getValue(0, "br_xml").toString().contains("? x m l"));
        assertFalse("BR xml is vernachelt", bericht.getValue(0, "br_xml").toString().startsWith(" < ? x m l"));

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal onroerende zaken klopt niet.", aantalBerichten, kad_onrrnd_zk.getRowCount());
    }
}
