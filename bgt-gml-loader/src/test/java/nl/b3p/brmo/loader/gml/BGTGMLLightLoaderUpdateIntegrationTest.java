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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.BIJWERKDATUM_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.assumeNotNull;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BGTGMLLightLoaderUpdateIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderUpdateIntegrationTest.class);

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
        ldr = new BGTGMLLightLoader();
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(BGTGMLLightLoaderUpdateIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(BGTGMLLightLoaderUpdateIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);

        clearTables();
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
    }

    /**
     * test scannen en laden van een directory zipfiles in bestaande tabellen,
     * de data wordt twee keer geladen zodat alle objecten de nieuwe datum
     * hebben.
     *
     * @throws Exception if any
     */
    @Test
    public void testUpdateFromDirectory() throws Exception {
        int load_one = 0;
        int load_two = 0;

        ldr.setScanDirectory(BGTGMLLightLoaderUpdateIntegrationTest.class.getResource("/gmllight/zips/").getFile());
        List<File> zips = ldr.scanDirectory();
        assertEquals("Verwacht aantal zipfiles", 1, zips.size());

        // eerste set laden met datum 3 dagen voor vandaag
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -3);
        ldr.setBijwerkDatum(cal.getTime());
        ldr.setLoadingUpdate(false);

        for (File zip : zips) {
            load_one = ldr.processZipFile(zip);
            assertTrue("Verwacht meer dan 1 geschreven feature", (load_one > 1));
        }

        Calendar today = Calendar.getInstance();
        ldr.setBijwerkDatum(today.getTime());
        ldr.setLoadingUpdate(true);
        for (File zip : zips) {
            load_two = ldr.processZipFile(zip);
            assertTrue("Verwacht meer dan 1 geschreven feature", (load_one > 1));
        }
        assertEquals("geladen en bijgwerkt zijn gelijk", load_one, load_two);

        SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMDD");

        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))) {

            connection.setAutoCommit(true);

            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                ResultSet res = connection.getMetaData().getTables(null, params.getProperty("schema"), t.name(), null);
                if (res.next()) {
                    String sql = "SELECT COUNT(ID_NAME) FROM " + params.getProperty("schema") + ".\"" + t.name()
                            + "\" WHERE " + BIJWERKDATUM_NAME + " < '" + fmt.format(today) + "';";
                    try {
                        ResultSet count = connection.createStatement().executeQuery(sql);
                        assertFalse("Verwacht geen oude data in tabel " + t.name(), count.next());
                    } catch (SQLException se) {
                        LOG.warn("Fout tijdens tellen in tabellen: " + se.getLocalizedMessage());
                    }
                }
            }
            connection.close();
        }
    }
}
