/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class BGTGMLLightLoaderUpdateIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderUpdateIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private final SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMdd");
    private BGTGMLLightLoader ldr;

    /**
     * set up test object.
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

        clearTables();
    }

    @AfterEach
    public void resetDatabase() throws Exception {
        sequential.unlock();
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
        assertEquals(1, zips.size(), "Verwacht aantal zipfiles");

        // eerste set laden met datum 21 dagen voor vandaag
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -21);

        for (File zip : zips) {
            load_one = ldr.processZipFile(zip);
            LOG.info("Totaal aantal ingevoegde features voor: " + zip.getName() + " is: " + load_one);
            assertTrue((load_one > 1), "Verwacht meer dan 1 geschreven feature");
            if (zip.getName().equalsIgnoreCase("extract-gmllight.zip")) {
                assertEquals(114, load_one, "Er zitten 114 objecten in de gml bestanden");
            }
        }
    }

    /**
     * test laden van twee in datum verschillende zipfiles, maar zelfde gebied,
     * in bestaande tabellen.
     *
     * @throws Exception if any
     */
    @Test
    @Disabled("De update zipfile zit nog te dicht op de bron, dus geen verschil.")
    public void testUpdateFromDirectoryTwoDates() throws Exception {
        int load_one, load_two;

        URL zipUrl = BGTGMLLightLoaderNederlandIntegrationTest.class.getResource("/gmllight/dated/38468_0-20160422.zip");
        assumeFalse(null == zipUrl, "Verwacht de zipfile met data te bestaan.");
        URL updateUrl = BGTGMLLightLoaderNederlandIntegrationTest.class.getResource("/gmllight/dated/38468_0-20160512.zip");
        assumeFalse(null == updateUrl, "Verwacht de zipfile met data te bestaan.");

        File zip = new File(zipUrl.getFile());
        assertNotNull(zip, "Zipfile is niet null");

        // eerste set laden als stand met datum 7 dagen voor vandaag
        Calendar cal = Calendar.getInstance();
        cal.set(2015, 4, 22, 17, 32);
        load_one = ldr.processZipFile(zip);
        assertTrue((load_one > 1), "Verwacht meer dan 1 geschreven feature");
    }
}
