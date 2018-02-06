/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.util.List;
import nl.egem.stuf.sector.bg._0204.ACDTabel;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author mprins
 */
public class StUFBGsynchroonIntegrationTest extends TestStub {

    private StUFBGsynchroon stub;

    @Before
    @Override
    public void setUp() {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        setupJNDI(dsRsgb, dsStaging);

        stub = new StUFBGsynchroon();
    }

    @After
    public void tearDown() {
        stub = null;
    }

    @Test
    @Ignore("Faalt met melding: Entiteitstype niet ondersteund: ACD")
    public void helloStUFBGsynchroon() throws Exception {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("ACD");

        VraagBericht v = new VraagBericht();
        v.setStuurgegevens(s);

        SynchroonAntwoordBericht a = stub.beantwoordSynchroneVraag(v);
        assertNotNull("Antwoord is null", a);
    }

    @Test(expected = StUFFout.class)
    public void foutStUFBGsynchroon() throws Exception {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("ACD");

        VraagBericht v = new VraagBericht();
        v.setStuurgegevens(s);

        SynchroonAntwoordBericht a = stub.beantwoordSynchroneVraag(v);
        assertNotNull("Antwoord is null", a);
    }
    
    @Test
    @Ignore("Faalt met melding: Entiteitstype niet ondersteund: ACD")
    public void testAntwoordBodyACD() throws StUFFout {
        
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("ACD");

        VraagBericht v = new VraagBericht();
        v.setStuurgegevens(s);

        SynchroonAntwoordBericht a = stub.beantwoordSynchroneVraag(v);
        Body b = a.getBody();
        List<ACDTabel> acd = b.getACD();
        assertNotNull(acd);
        assertEquals(1,acd.size());
        ACDTabel t = acd.get(0);
        //assertEquals("pietje", t.getOmschrijving().getValue().getValue());
    }
}
