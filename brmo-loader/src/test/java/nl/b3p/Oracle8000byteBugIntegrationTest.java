/*
 * Copyright (C) 2017 B3Partners B.V.
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
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Testcases voor mantis-6512 en mantis-6727; Draaien met:
 * {@code mvn -Dit.test=Oracle8000byteBugIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-loader > /tmp/oracle.log}
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
@Tag("not-mssql")
@Tag("not-pgsql")
public class Oracle8000byteBugIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Oracle8000byteBugIntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"type","filename", brxml bytes, aantalBerichten, aantalProcessen, "datumEersteMutatie"},
                arguments("brk", "/8000byteBericht/8000bytesBRxml.xml", 8001, 1, 1, "2017-08-02 00:00:00.0"),
                arguments("brk", "/8000byteBericht/8001bytesBRxml.xml", 8001, 1, 1, "2017-08-02 00:00:00.0"),
                arguments("brk", "/8000byteBericht/7999bytesBRxml.xml", 7999, 1, 1, "2017-08-02 00:00:00.0"),
                arguments("brk", "/8000byteBericht/PD_MUTBX01_23-8-2017.xml", 8093, 1, 1, "2017-08-02 00:00:00.0"),
                arguments("brk", "/8000byteBericht/MUTBX01.xml", 8093, 1, 1, "2017-07-03 00:00:00.0")
        );
    }

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        assumeTrue(this.isOracle, "Deze test is alleen voor Oracle.");

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
        assumeTrue(0L == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (brmo != null) {
            brmo.closeBrmoFramework();
        }

        if (rsgb != null) {
            CleanUtil.cleanRSGB_BRK(rsgb, true);
            rsgb.close();
        }

        if (staging != null) {
            CleanUtil.cleanSTAGING(staging, false);
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed");
        }
    }

    @ParameterizedTest(name = "{index}: bestand: {1}")
    @MethodSource("argumentsProvider")
    public void testBrkXMLToStaging(String bestandType, String bestandNaam, int brxml_bytes, long aantalBerichten, long aantalProcessen, String datumEersteMutatie) throws Exception {
        assumeFalse(null == Oracle8000byteBugIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(bestandType, Oracle8000byteBugIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals(aantalBerichten, bericht.getRowCount(), "Het aantal berichten klopt niet");
        assertEquals(datumEersteMutatie, bericht.getValue(0, "datum").toString(), "De datum klopt niet");

        assertEquals(brxml_bytes, bericht.getValue(0, "br_xml").toString().getBytes().length,
                "incorrect aantal bytes");
        assertNull(bericht.getValue(0, "db_xml"), "DB xml is niet null (want nog niet getransformeerd)");
        assertFalse(bericht.getValue(0, "br_xml").toString().contains("? x m l"), "BR xml is vernachelt");
        assertFalse(bericht.getValue(0, "br_xml").toString().startsWith(" < ? x m l"),
                "BR xml is vernachelt");

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");

        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
            // test toepassing van de 8000byte HACK in de stagingproxy
            int dflt = b.getDbXml().getBytes().length;
            int utf8 = b.getDbXml().getBytes(StandardCharsets.UTF_8).length;
            assertNotEquals(8000, dflt, "db xml is 8000 bytes");
            assertNotEquals(8000, utf8, "db xml in UTF8 is 8000 bytes");

            dflt = b.getBrXml().getBytes().length;
            utf8 = b.getBrXml().getBytes(StandardCharsets.UTF_8).length;
            assertNotEquals(8000, dflt, "br xml is 8000 bytes");
            assertNotEquals(8000, utf8, "br xml in UTF8 is 8000 bytes");
        }

        bericht = staging.createDataSet().getTable("bericht");
        assertEquals(aantalBerichten, bericht.getRowCount(), "Het aantal berichten");
        assertEquals(brxml_bytes, bericht.getValue(0, "br_xml").toString().getBytes().length, "8000 bytes");
        assertFalse(bericht.getValue(0, "br_xml").toString().contains("? x m l"), "BR xml is vernachelt");
        assertFalse(bericht.getValue(0, "br_xml").toString().startsWith(" < ? x m l"),
                "BR xml is vernachelt");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(aantalBerichten, kad_onrrnd_zk.getRowCount(), "Het aantal onroerende zaken klopt niet.");
    }
}
