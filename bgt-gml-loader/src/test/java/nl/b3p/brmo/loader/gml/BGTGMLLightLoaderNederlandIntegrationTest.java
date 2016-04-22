/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class BGTGMLLightLoaderNederlandIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderNederlandIntegrationTest.class);

    private BGTGMLLightLoader ldr;
    private final Properties params = new Properties();

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
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
        params.load(BGTGMLLightLoaderNederlandIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(BGTGMLLightLoaderNederlandIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);
    }

    // @After
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
     * test laden van een zipfile van heel NL in bestaande database, de file was
     * ~3.2Gb op 5 april 2016.
     *
     * @throws Exception if any
     */
    @Test
    public void testLaadNederland() throws Exception {
        URL zipUrl = BGTGMLLightLoaderNederlandIntegrationTest.class.getResource("/nederland/NL-BGT-gmllight.zip");
        assumeNotNull("Verwacht de zipfile met data van heel NL te bestaan.", zipUrl);

        clearTables();

        File zip = new File(zipUrl.getFile());

        assertNotNull("Zipfiles is niet null", zip);

        int actual = ldr.processZipFile(zip);
        assertTrue("Verwacht meer dan 1 geschreven feature", (actual > 1));
    }

    /**
     * test laden van een set zipfiles van heel NL in bestaande database.
     *
     * @throws Exception if any
     */
    @Test
    public void testLaadNederlandMultiZip() throws Exception {
        String listDir = BGTGMLLightLoaderIntegrationTest.class.getResource("/nederlandmultizip/").getFile();
        File dir = new File(listDir);
        assumeNotNull("Verwacht de directory met data van heel NL te bestaan.", dir);
        assumeTrue("Verwacht de directory met data van heel NL te bestaan.", dir.isDirectory());

        ldr.setScanDirectory(dir);
        List<File> zips = ldr.scanDirectory();

        assumeFalse("Verwacht meer dan 0 zipfiles om te verwerken.", zips.isEmpty());

        clearTables();
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue("Verwacht meer dan 1 geschreven feature per zipfile", (actual > 1));
        }
    }
}
