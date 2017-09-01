/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.FoutBericht.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
@WebService(
        serviceName = "StUFBGSynchroon",
        portName = "StUFBGSynchronePort",
        endpointInterface = "nl.egem.stuf.sector.bg._0204.StUFBGSynchroonPortType",
        targetNamespace = "http://www.egem.nl/StUF/sector/bg/0204",
        wsdlLocation = "WEB-INF/wsdl/bg0204.wsdl"
)
@HandlerChain(file = "/handler-chain.xml")
public class StUFBGsynchroon {

    private static final Log LOG = LogFactory.getLog(StUFBGsynchroon.class);

    public SynchroonAntwoordBericht beantwoordSynchroneVraag(VraagBericht vraag) throws StUFFout {
        LOG.debug("Er is antwoord ontvangen van soort: " + vraag.getStuurgegevens().getBerichtsoort());

        SynchroonAntwoordBericht antw = new SynchroonAntwoordBericht();

        FoutBericht fout = new FoutBericht();
        fout.setStuurgegevens(vraag.getStuurgegevens());
        fout.setBody(new Body());
        throw new StUFFout("Not implemented yet.", fout);
    }

}
