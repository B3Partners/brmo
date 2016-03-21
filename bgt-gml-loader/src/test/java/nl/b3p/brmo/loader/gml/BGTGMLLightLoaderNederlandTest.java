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
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import org.junit.BeforeClass;

public class BGTGMLLightLoaderNederlandTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderNederlandTest.class);

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
        params.load(BGTGMLLightLoaderNederlandTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(BGTGMLLightLoaderNederlandTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        ldr.setDbConnProps(params);
    }

    //@After
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
                        connection.createStatement().executeQuery(sql);
                    } catch (SQLException se) {
                        LOG.warn("Mogelijke fout tijdens legen van tabellen: " + se.getLocalizedMessage());
                    }
                }
            }
            connection.close();
        }
    }

    /**
     * test laden van een zipfile van heel NL. de file is 3.2Gb
     *
     * @throws Exception if any
     */
    @Test
    public void testLaadNederland() throws Exception {
        URL zipUrl = BGTGMLLightLoaderNederlandTest.class.getResource("/nederland/NL-BGT-gmllight.zip");
        assumeNotNull("Verwacht de zipfile met data van heel NL te bestaan.", zipUrl);

        clearTables();

        File zip = new File(zipUrl.getFile());

        assertNotNull("Zipfiles is niet null", zip);

        int actual = ldr.processZipFile(zip);
        assertTrue("Verwacht meer dan 1 geschreven feature", (actual > 1));
    }
}
