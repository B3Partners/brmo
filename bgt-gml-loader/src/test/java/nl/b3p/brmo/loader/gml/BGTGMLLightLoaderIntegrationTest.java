/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
     * set up test object BGTGMLLightLoader.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    public void setUp() throws Exception {
        loadProps();

        sequential.lock();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);
        ldr.truncateTables();

    }

    @AfterEach
    public void cleanUp() throws Exception {
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
        assertEquals(1, ldr.processGMLFile(gml), "Aantal geschreven features");
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
        assertEquals(10 - 2, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessWijkGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_wijk.gml").toURI());
        assertEquals(4, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessStadsdeelGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_stadsdeel.gml").toURI());
        assertEquals(3, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessOpenbareRuimteGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_openbareruimte.gml").toURI());
        assertEquals(26, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessWaterschapGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/bgt_waterschap.gml").toURI());
        assertEquals(1, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessDuplicateGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/duplicate/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals(2, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test parsen en laden van 1 bestand in bestaande tabel.
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessOutOFTimeGMLFile() throws Exception {
        // bevat 1 historisch record en 1 toekomstig record
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/outoftime/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals(1, ldr.processGMLFile(gml), "Aantal geschreven features");
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
        assertEquals(1, zips.size(), "Verwacht aantal zipfiles");
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue((actual > 1), "Verwacht meer dan 1 geschreven feature");
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
        assertEquals(1, zips.size(), "Verwacht aantal zipfiles");
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue((actual > 1), "Verwacht meer dan 1 geschreven feature");
        }
    }
}
