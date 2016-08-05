/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author mprins
 */
public class BGTGMLLightLoaderCreateTablesIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderCreateTablesIntegrationTest.class);

    private BGTGMLLightLoader ldr;

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    public void setUp() throws Exception {
        loadProps();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        dropTables();
    }

    /**
     * test voor: tabellen bestaan niet in database en createSchema is false
     * (default).
     *
     * @throws Exception if any
     */
    @Test(expected = IllegalStateException.class)
    @Ignore("Overslaan omdat tabellen in db worden gemaakt met SQL.")
    public void testProcessGMLFileFail() throws Exception {
        ldr.setCreateTables(false);
        @SuppressWarnings("unused")
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/one/bgt_onbegroeidterreindeel.gml").toURI());
        fail("De verwachte exceptie is niet opgetreden.");
    }

    /**
     * test voor: tabellen bestaan niet in database en createSchema is true
     * (default).
     *
     * @throws Exception if any
     */
    @Test
    @Ignore("Overslaan omdat tabellen in db worden gemaakt met SQL.")
    public void testProcessGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/one/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals("Aantal geschreven features", 1, ldr.processGMLFile(gml));
    }

    /**
     * test scannen en laden van een directory zipfiles in niet bestaande
     * tabellen.
     *
     * @throws Exception if any
     */
    @Test
    @Ignore("Overslaan omdat tabellen in db worden gemaakt met SQL.")
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
