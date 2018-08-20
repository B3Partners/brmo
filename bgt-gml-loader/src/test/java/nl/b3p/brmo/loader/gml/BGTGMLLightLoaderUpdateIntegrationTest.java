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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.ID_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import org.junit.Ignore;

public class BGTGMLLightLoaderUpdateIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderUpdateIntegrationTest.class);

    private BGTGMLLightLoader ldr;

    private final Lock sequential = new ReentrantLock();

    private final SimpleDateFormat fmt = new SimpleDateFormat("YYYYMMdd");

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
        assertEquals("Verwacht aantal zipfiles", 1, zips.size());

        // eerste set laden met datum 21 dagen voor vandaag
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -21);

        for (File zip : zips) {
            load_one = ldr.processZipFile(zip);
            LOG.info("Totaal aantal ingevoegde features voor: " + zip.getName() + " is: " + load_one);
            assertTrue("Verwacht meer dan 1 geschreven feature", (load_one > 1));
            if (zip.getName().equalsIgnoreCase("extract-gmllight.zip")) {
                assertEquals("Er zitten 114 objecten in de gml bestanden", 114, load_one);
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
    @Ignore("De update zipfile zit nog te dicht op de bron, dus geen verschil.")
    public void testUpdateFromDirectoryTwoDates() throws Exception {
        int load_one, load_two;

        URL zipUrl = BGTGMLLightLoaderNederlandIntegrationTest.class.getResource("/gmllight/dated/38468_0-20160422.zip");
        assumeNotNull("Verwacht de zipfile met data te bestaan.", zipUrl);
        URL updateUrl = BGTGMLLightLoaderNederlandIntegrationTest.class.getResource("/gmllight/dated/38468_0-20160512.zip");
        assumeNotNull("Verwacht de zipfile met data te bestaan.", zipUrl);

        File zip = new File(zipUrl.getFile());
        assertNotNull("Zipfile is niet null", zip);

        // eerste set laden als stand met datum 7 dagen voor vandaag
        Calendar cal = Calendar.getInstance();
        cal.set(2015, 4, 22, 17, 32);
        load_one = ldr.processZipFile(zip);
        assertTrue("Verwacht meer dan 1 geschreven feature", (load_one > 1));
    }
}
