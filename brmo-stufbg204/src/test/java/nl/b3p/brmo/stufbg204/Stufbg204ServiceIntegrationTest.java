/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht;
import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht.Body;
import nl.egem.stuf.stuf0204.BevestigingsBericht;
import nl.egem.stuf.stuf0204.Mutatiesoort;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import nl.egem.stuf.stuf0204.Stuurgegevens.Kennisgeving;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author Mark Prins
 */
public class Stufbg204ServiceIntegrationTest extends TestStub {

    private StUFBGasynchroon service;


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

        service = new StUFBGasynchroon();
    }

    @After
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
        assertNotNull("BevestigingsBericht is null.", b);
        assertEquals("Verwacht 'BRMO'", "BRMO", b.getStuurgegevens().getZender().getApplicatie());
    }
}
