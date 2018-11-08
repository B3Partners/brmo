/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.bgt.util;

import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geotools.util.factory.GeoTools;
import org.junit.*;

/**
 * Testcases voor {@link PDOKBGTLightUtil}.
 *
 * @author mprins
 */
public class PDOKBGTLightUtilTest {

    @BeforeClass
    public static void initGeotools() {
        GeoTools.init();
    }
    /**
     * offline test.
     */
    @Test
    public void calculateGridIdsOfflineTest() {
        Set<Integer> ids = PDOKBGTLightUtil.calculateGridIds(
                /* dit vlakt overlap met cel 38725 in het testbestand, maar niet met andere cellen */
                "POLYGON ((143850 487030, 149760 486760, 149470 490950, 143600 491000, 143850 487030))",
                PDOKBGTLightUtilTest.class.getResource("/geojson/tileinfo.json").toString()
        );
        assertTrue("Er zit 1 element in de lijst met ids", ids.size() == 1);
        assertEquals("Het element in de lijst met ids is", 38725, ids.iterator().next().intValue());
    }

    /**
     * Online test.
     */
    @Test
    public void calculateGridIdsOnlineTest() {
        TreeSet<Integer> ids = (TreeSet<Integer>) PDOKBGTLightUtil.calculateGridIds(
                /* dit is de geometrie van grid cel 38725 */
                "POLYGON ((146000 488000, 148000 488000, 148000 490000, 146000 490000, 146000 488000))",
                "http://files.b3p.nl/brmo/bgt/tileinfo.geojson"
        );
        assertTrue("Er zitten 9 elementen in de lijst met ids", ids.size() == 9);
        assertEquals("Het element in de lijst met ids is", 38382, ids.first().intValue());
    }
}
