/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
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
    @BeforeEach
    public void setUp() throws Exception {
        loadProps();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        dropTables();
    }

    /**
     * test voor: tabellen bestaan niet in database en createSchema is false
     * (default).
     */
    @Test
    @Disabled("Overslaan omdat tabellen in db worden gemaakt met SQL.")
    public void testProcessGMLFileFail() {
        assertThrows(IllegalStateException.class, () -> {
            ldr.setCreateTables(false);
            @SuppressWarnings("unused")
            File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/one/bgt_onbegroeidterreindeel.gml").toURI());
            fail("De verwachte exceptie is niet opgetreden.");
        });
    }

    /**
     * test voor: tabellen bestaan niet in database en createSchema is true
     * (default).
     *
     * @throws Exception if any
     */
    @Test
    @Disabled("Overslaan omdat tabellen in db worden gemaakt met SQL.")
    public void testProcessGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/one/bgt_onbegroeidterreindeel.gml").toURI());
        assertEquals(1, ldr.processGMLFile(gml), "Aantal geschreven features");
    }

    /**
     * test scannen en laden van een directory zipfiles in niet bestaande
     * tabellen.
     *
     * @throws Exception if any
     */
    @Test
    @Disabled("Overslaan omdat tabellen in db worden gemaakt met SQL.")
    public void testScanDirectory() throws Exception {
        ldr.setScanDirectory(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/zips/").getFile());
        List<File> zips = ldr.scanDirectory();
        assertEquals(1, zips.size(), "Verwacht aantal zipfiles");
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue((actual > 1), "Verwacht meer dan 1 geschreven feature");
        }
    }
}
