/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.bgt.util;

import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcases voor {@link PDOKBGTLightUtil}.
 *
 * @author mprins
 */
public class PDOKBGTLightUtilTest {

    /**
     * offline test.
     */
    @Test
    public void calculateGridIdsTest() {
        TreeSet<Integer> ids = (TreeSet) PDOKBGTLightUtil.calculateGridIds("POLYGON ((143850 487030, 149760 486760, 149470 490950, 143600 491000, 143850 487030))",
                PDOKBGTLightUtilTest.class.getResource("/geojson/tileinfo.json").toString()
        );
        assertTrue("Er zit 1 element in de lijst met ids", ids.size() == 1);
        assertEquals("Het element in de lijst met ids is", 38725, ids.first().intValue());
    }

    /**
     * online test.
     */
    @Test
    public void calculateGridIdsOnlineTest() {
        TreeSet<Integer> ids = (TreeSet) PDOKBGTLightUtil.calculateGridIds(
                /* dit is de geometrie van grid cel 38725 */
                "POLYGON ((146000 488000, 148000 488000, 148000 490000, 146000 490000, 146000 488000))",
                "https://www.pdok.nl/download/service/tileinfo.json?dataset=bgt&format=citygml"
        );
        assertTrue("Er zitten 9 elementen in de lijst met ids", ids.size() == 9);
        assertEquals("Het element in de lijst met ids is", 38382, ids.first().intValue());
    }
}
