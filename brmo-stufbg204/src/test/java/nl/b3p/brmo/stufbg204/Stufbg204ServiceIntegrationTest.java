/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht;
import nl.egem.stuf.stuf0204.BevestigingsBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
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
    public void hello() {
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("test");
        s.setEntiteittype("test");

        KennisgevingsBericht k = new KennisgevingsBericht();
        k.setStuurgegevens(s);

        BevestigingsBericht b = service.ontvangKennisgeving(k);
        assertNotNull("BevestigingsBericht is null.", b);
        assertEquals("Verwacht 'BRMO'", "BRMO", b.getStuurgegevens().getZender().getApplicatie());
    }
}
