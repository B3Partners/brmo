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
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.ID_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.fail;

/**
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BGTNullGeomIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTNullGeomIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: gml:{0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            // arrays van: {"gmlFileName", "tabel",expectedNumOfElements, expectedKruinlijnElements },
            {"/gmllight/kruinlijntest/bgt_ondersteunendwegdeel.gml", "ondersteunend_wegdeel",
                431 - 28 /* duplicaten */ - 6 /* vervallen of nog niet bestaand */,
                2 /* kruinlijn elementen */},
            {"/gmllight/mantis6028/bgt_begroeidterreindeel.gml", "begroeid_terreindeel", 1, 1}
        });
    }

    private final String gmlFileName;
    private final String tableName;
    private final int expectedNumOfElements;
    private final int expectedKruinlijnElements;
    private final Lock sequential = new ReentrantLock();
    private BGTGMLLightLoader ldr;

    /**
     *
     * @param gmlFileName filename of the test resource (GML)
     * @param tableName tabel naam
     * @param expectedNumOfElements number of elements in resources
     * @param expectedKruinlijnElements aantal kruinlijn elementen
     *
     * @see #params()
     */
    public BGTNullGeomIntegrationTest(String gmlFileName, String tableName, int expectedNumOfElements, int expectedKruinlijnElements) {
        this.gmlFileName = gmlFileName;
        this.tableName = tableName;
        this.expectedNumOfElements = expectedNumOfElements;
        this.expectedKruinlijnElements = expectedKruinlijnElements;
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    public void setUp() throws Exception {
        loadProps();

        sequential.lock();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);

        clearTables();
    }

    @After
    public void cleanUp() throws Exception {
        clearTables();
        sequential.unlock();
    }


    /**
     * test parsen en laden van 1 GML bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessGMLFile() throws Exception {
        if (isMsSQL && gmlFileName.equalsIgnoreCase("/gmllight/mantis6028/bgt_begroeidterreindeel.gml")) {
            // overslaan vanwege https://osgeo-org.atlassian.net/browse/GEOT-5512
            return;
        }

        int nullGeomCount = expectedNumOfElements - expectedKruinlijnElements;

        File gml = new File(BGTLightKruinlijnIntegrationTest.class.getResource(gmlFileName).toURI());
        int actualElements = ldr.processGMLFile(gml);

        assertEquals(BGTGMLLightLoader.STATUS.OK, ldr.getStatus());
        assertEquals("Aantal geschreven features", expectedNumOfElements, actualElements);

        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))) {

            connection.setAutoCommit(true);

            String sql = "SELECT COUNT(" + ID_NAME + ") FROM \""
                    + params.getProperty("schema")
                    + "\".\"" + tableName + "\" WHERE kruinlijn is null";
            sql = isOracle ? sql.toUpperCase() : sql;

            try {
                ResultSet count = connection.createStatement().executeQuery(sql);
                count.next();
                int counted = count.getInt(1);
                assertEquals("Aantal 'null' kruinlijn elementen", nullGeomCount, counted);
            } catch (SQLException se) {
                LOG.error("Fout tijdens tellen 'null' kruinlijn elementen: ", se);
                fail("Fout tijdens tellen 'null' kruinlijn elementen: " + se.getLocalizedMessage());
            }
            connection.close();
        }

    }
}
