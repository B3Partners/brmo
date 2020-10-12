/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.entity;

import nl.b3p.brmo.loader.xml.BagXMLReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author mprins
 */
public class BagBerichtTest {

    private static final Log LOG = LogFactory.getLog(BagBerichtTest.class);

    private BagXMLReader bReader;

    private static final String B1 = "/GH-280/b1.xml";
    private static final String B2 = "/GH-280/b2.xml";

    @AfterEach
    public void cleanup() throws Exception {
        bReader = null;
    }

    @Test
    public void dateTimeUnequalTest() throws Exception {
        BagBericht b1;
        BagBericht b2;

        bReader = new BagXMLReader(BagBerichtTest.class.getResourceAsStream(B1));
        assertTrue(bReader.hasNext());
        b1 = bReader.next();

        bReader = new BagXMLReader(BagBerichtTest.class.getResourceAsStream(B2));
        assertTrue(bReader.hasNext());
        b2 = bReader.next();

        LOG.debug("bericht 1: " + b1);
        LOG.debug("bericht 2: " + b2);
        assertFalse(b1.getObjectRef().equals(b2.getObjectRef())
                && b1.getDatum().equals(b2.getDatum())
                && b1.getVolgordeNummer().equals(b2.getVolgordeNummer()), "Objecten zijn gelijk");
    }

    @Test
    public void dateTimeEqualTest() throws Exception {
        BagXMLReader bReader;
        BagBericht b1;
        BagBericht b2;

        bReader = new BagXMLReader(BagBerichtTest.class.getResourceAsStream(B1));
        assertTrue(bReader.hasNext());
        b1 = bReader.next();

        bReader = new BagXMLReader(BagBerichtTest.class.getResourceAsStream(B1));
        assertTrue(bReader.hasNext());
        b2 = bReader.next();

        LOG.debug("bericht 1: " + b1);
        LOG.debug("bericht 2: " + b2);
        assertTrue(
                (b1.getObjectRef().equals(b2.getObjectRef())
                && b1.getDatum().equals(b2.getDatum())
                && b1.getVolgordeNummer().equals(b2.getVolgordeNummer())),
                "Objecten zijn niet gelijk");
    }
}
