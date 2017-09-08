/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht;
import nl.egem.stuf.stuf0204.BevestigingsBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class Stufbg204ServiceIntegrationTest {

    private StUFBGasynchroon service;


    @Before
    public void setUp() {
        service = new StUFBGasynchroon();
    }

    @After
    public void tearDown() {
        service = null;
    }
    
    @Test
    public void hello() {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("test");

        KennisgevingsBericht k = new KennisgevingsBericht();
        k.setStuurgegevens(s);

        BevestigingsBericht b = service.ontvangKennisgeving(k);
        assertNotNull("BevestigingsBericht is null.", b);

    }
}
