/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.ACDTabel;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mprins
 */
public class StUFBGsynchroonIntegrationTest extends TestStub {

    private StUFBGsynchroon stub;

    @BeforeEach
    @Override
    public void setUp() {
        stub = new StUFBGsynchroon();
    }

    @AfterEach
    public void tearDown() {
        stub = null;
    }

    @Test
    @Disabled("Faalt met melding: Entiteitstype niet ondersteund: ACD")
    public void helloStUFBGsynchroon() throws Exception {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("ACD");

        VraagBericht v = new VraagBericht();
        v.setStuurgegevens(s);

        SynchroonAntwoordBericht a = stub.beantwoordSynchroneVraag(v);
        assertNotNull(a, "Antwoord is null");
    }

    @Test
    public void foutStUFBGsynchroon(){
        assertThrows(StUFFout.class, () -> {
            Stuurgegevens s = new Stuurgegevens();
            s.setBerichtsoort("test");
            s.setEntiteittype("ACD");

            VraagBericht v = new VraagBericht();
            v.setStuurgegevens(s);

            SynchroonAntwoordBericht a = stub.beantwoordSynchroneVraag(v);
            assertNotNull(a, "Antwoord is null");
        });
    }
    
    @Test
    @Disabled("Faalt met melding: Entiteitstype niet ondersteund: ACD")
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
        assertEquals("pietje", t.getOmschrijving().getValue().getValue());
    }
}
