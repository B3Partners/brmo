/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author mprins
 */
public class StUFBGsynchroonIntegrationTest {

    private StUFBGsynchroon stub;

    @Before
    public void setUp() {
        stub = new StUFBGsynchroon();
    }

    @After
    public void tearDown() {
        stub = null;
    }

    @Test
    public void hello() throws StUFFout {
        VraagBericht vraag = new VraagBericht();
        vraag.getStuurgegevens().setBerichtsoort("test");
        vraag.getStuurgegevens().setEntiteittype("test");
        SynchroonAntwoordBericht antw = stub.beantwoordSynchroneVraag(vraag);
        assertNotNull("Antwoord is null", antw);
    }
}
