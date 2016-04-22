/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BGTGMLLightLoaderIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderIntegrationTest.class);

    private BGTGMLLightLoader ldr;
    private final Properties params = new Properties();

    private final Lock sequential = new ReentrantLock();

    @Rule
    public TestName name = new TestName();

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
    }

    @Before
    public void logStart() {
        LOG.info("test start: " + name.getMethodName());
    }

    @After
    public void logEnd() {
        LOG.info("test einde: " + name.getMethodName());
    }
    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    public void setUp() throws Exception {
        sequential.lock();


        ldr = new BGTGMLLightLoader();
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(BGTGMLLightLoaderIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(BGTGMLLightLoaderIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);
    }

    @After
    public void clearTables() throws Exception {
        try {
            Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            fail("Laden van database driver (" + params.getProperty("jdbc.driverClassName") + ") is mislukt.");
        }
        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))) {

            connection.setAutoCommit(true);

            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                ResultSet res = connection.getMetaData().getTables(null, params.getProperty("schema"), t.name(), null);
                if (res.next()) {
                    String sql = "DELETE FROM " + params.getProperty("schema") + ".\"" + t.name() + "\";";
                    LOG.info("legen tabel: " + params.getProperty("schema") + "." + t.name() + " met sql: " + sql);
                    try {
                        connection.createStatement().executeUpdate(sql);
                    } catch (SQLException se) {
                        LOG.warn("Mogelijke fout tijdens legen van tabellen: " + se.getLocalizedMessage());
                    }
                }
            }
            connection.close();
        }
        sequential.unlock();
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/one/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals("Aantal geschreven features", 1, ldr.processGMLFile(gml));
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessDuplicateGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/duplicate/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals("Aantal geschreven features", 2, ldr.processGMLFile(gml));
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessOutOFTimeGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/outoftime/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals("Aantal geschreven features", 0, ldr.processGMLFile(gml));
    }

    /**
     * test scannen en laden van een directory zipfiles in bestaande tabellen.
     *
     * @throws Exception if any
     */
    @Test
    public void testScanDirectory() throws Exception {
        ldr.setScanDirectory(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/zips/").getFile());
        List<File> zips = ldr.scanDirectory();
        assertEquals("Verwacht aantal zipfiles", 1, zips.size());
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue("Verwacht meer dan 1 geschreven feature", (actual > 1));
        }
    }
}
