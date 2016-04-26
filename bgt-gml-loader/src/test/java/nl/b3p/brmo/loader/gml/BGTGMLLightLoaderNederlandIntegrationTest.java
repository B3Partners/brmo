/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class BGTGMLLightLoaderNederlandIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderNederlandIntegrationTest.class);

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
        assumeNotNull("Verwacht de zipfile met data van heel NL te bestaan.", zipUrl);

        clearTables();

        File zip = new File(zipUrl.getFile());
        assertNotNull("Zipfile is niet null", zip);

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
