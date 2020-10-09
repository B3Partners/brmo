/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.bgt.util;

import org.geotools.util.factory.GeoTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testcases voor {@link PDOKBGTLightUtil}.
 *
 * @author mprins
 */
public class PDOKBGTLightUtilTest {

    @BeforeAll
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
        assertTrue(ids.size() == 1, "Er zit 1 element in de lijst met ids");
        assertEquals(38725, ids.iterator().next().intValue(), "Het element in de lijst met ids is");
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
        assertTrue(ids.size() == 9, "Er zitten 9 elementen in de lijst met ids");
        assertEquals(38382, ids.first().intValue(), "Het element in de lijst met ids is");
    }
}
