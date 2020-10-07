/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht;
import nl.egem.stuf.stuf0204.BevestigingsBericht;
import nl.egem.stuf.stuf0204.Mutatiesoort;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import nl.egem.stuf.stuf0204.Stuurgegevens.Kennisgeving;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 *
 * @author Mark Prins
 */
public class Stufbg204ServiceIntegrationTest extends TestStub {

    private StUFBGasynchroon service;

    @BeforeEach
    @Override
    public void setUp() {
        service = new StUFBGasynchroon();
    }

    @AfterEach
    public void tearDown() {
        service = null;
    }
    
    @Test
    public void testOntvangKennisgeving() {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("test");
        Kennisgeving k = new Kennisgeving();
        k.setMutatiesoort(Mutatiesoort.V);
        s.setKennisgeving(k);
        KennisgevingsBericht kb = new KennisgevingsBericht();
        kb.setStuurgegevens(s);

        BevestigingsBericht b = service.ontvangKennisgeving(kb);
        assertNotNull(b, "BevestigingsBericht is null.");
        assertEquals("BRMO", b.getStuurgegevens().getZender().getApplicatie(), "Verwacht 'BRMO'");
    }
}
