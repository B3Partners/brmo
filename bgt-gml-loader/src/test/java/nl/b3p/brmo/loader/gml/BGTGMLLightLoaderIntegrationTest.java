/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testcases voor {@link BGTGMLLightLoader}. Om de tests te runnen gebruik je:
 * {@code mvn -Dit.test=BGTGMLLightLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=BGTGMLLightLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Ppostgresql > target/postgresql.log}
 * voor Postgis.
 *
 * @author mprins
 */
public class BGTGMLLightLoaderIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderIntegrationTest.class);

    private BGTGMLLightLoader ldr;

    private final Lock sequential = new ReentrantLock();

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

//        clearTables();
    }

    @After
    public void cleanUp() throws Exception {
//        clearTables();
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
    public void testProcessBuurtGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_buurt.gml").toURI());
        // er zitten 10 buurten waarvan er 2 vervallen zijn
        assertEquals("Aantal geschreven features", 10 - 2, ldr.processGMLFile(gml));
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessWijkGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_wijk.gml").toURI());
        assertEquals("Aantal geschreven features", 4, ldr.processGMLFile(gml));
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessOpenbareRuimteGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_openbareruimte.gml").toURI());
        assertEquals("Aantal geschreven features", 26, ldr.processGMLFile(gml));
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

    /**
     * test scannen en laden van een directory zipfiles in bestaande tabellen.
     *
     * @throws Exception if any
     */
    @Test
    public void testScanDirectoryTwo() throws Exception {
        ldr.setScanDirectory(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/two/").getFile());
        List<File> zips = ldr.scanDirectory();
        assertEquals("Verwacht aantal zipfiles", 1, zips.size());
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue("Verwacht meer dan 1 geschreven feature", (actual > 1));
        }
    }
}
