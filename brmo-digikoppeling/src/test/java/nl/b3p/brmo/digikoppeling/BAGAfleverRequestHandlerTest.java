/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BAGAfleverRequestHandlerTest {

    private AfleverRequest verzoek;
    private AfleverResponse antwoord;
    private BAGAfleverRequestHandler instance;

    private static final String AANLEVERKENMERK = "Aanleverkenmerk";

    @Before
    public void setUp() {
        instance = new BAGAfleverRequestHandler("target/test/BAGAfleverRequestHandlerTest");

        verzoek = new AfleverRequest();

        antwoord = new AfleverResponse();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of handle method, of class BAGAfleverRequestHandler.
     *
     * @todo implementatie
     */
    @Test
    @Ignore("implementeren als duidelijk is wat precies in het antwoord moet zitten..")
    public void testHandle() {
        instance.handle(verzoek, antwoord);

        fail("The test case is a prototype.");
    }

}
