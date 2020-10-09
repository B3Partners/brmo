/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BGTGMLLightLoaderNederlandIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderNederlandIntegrationTest.class);

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
        ldr.setCreateTables(false);
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
        assumeFalse(null == zipUrl, "Verwacht de zipfile met data van heel NL te bestaan.");
        clearTables();

        File zip = new File(zipUrl.getFile());
        assertNotNull(zip, "Zipfile is niet null");

        int actual = ldr.processZipFile(zip);
        assertTrue((actual > 1), "Verwacht meer dan 1 geschreven feature");
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
        assumeFalse(null == dir, "Verwacht de directory met data van heel NL te bestaan.");
        assumeTrue(dir.isDirectory(), "Verwacht de directory met data van heel NL te bestaan.");

        ldr.setScanDirectory(dir);
        List<File> zips = ldr.scanDirectory();

        assumeFalse(zips.isEmpty(), "Verwacht meer dan 0 zipfiles om te verwerken.");

        clearTables();
        for (File zip : zips) {
            int actual = ldr.processZipFile(zip);
            assertTrue((actual > 1), "Verwacht meer dan 1 geschreven feature per zipfile");
        }
    }
}
