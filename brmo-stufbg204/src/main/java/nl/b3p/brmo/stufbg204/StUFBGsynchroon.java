/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.util.List;
import java.util.Map;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import nl.b3p.brmo.stufbg204.util.StUFbg204Util;
import nl.egem.stuf.sector.bg._0204.PRSVraag;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
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
        try {
            LOG.debug("Er is antwoord ontvangen van soort: " + vraag.getStuurgegevens().getBerichtsoort());
            SynchroonAntwoordBericht antw = new SynchroonAntwoordBericht();
            antw.setStuurgegevens(StUFbg204Util.maakStuurgegevens(vraag.getStuurgegevens()));
            Body b = process(vraag);
            antw.setBody(b);
            return antw;
        } catch (Exception e) {
            FoutBericht fout = StUFbg204Util.maakFout();
            throw new StUFFout("Not implemented yet.", fout, e);
        }
    }
    
    private Body process(VraagBericht vraag){
        // interpreteer vraag
        String q = createQuery(vraag);
        // haal resultaten op
        List<Map<String,Object>> results = getResults(q, vraag);
        // Sorteer resultaten
        sort(results, vraag);
        // maak entities adhv gevraagde elementen
        List<Object> res = createResults(results, vraag);
        // stuur antwoord
        Body b = createBody(res);
        return b;
    }
    
    private String createQuery(VraagBericht vraag){
        
        Stuurgegevens sg = vraag.getStuurgegevens();
        String q = "select * from ";
        String entiteitType = sg.getEntiteittype();
        nl.egem.stuf.sector.bg._0204.VraagBericht.Body b = vraag.getBody();
        switch (entiteitType) {
            case "PRS": {
                q += "ingeschr_nat_prs inp inner join subject s on inp.sc_identif = s.identif inner join nat_prs np on np.sc_identif = s.identif";
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        // Haal op wat de gevraagde entiteit is
            // haal de rsgb tabellen op
        // Haal op wat het criterium is
        CriteriaParser cp = new CriteriaParser();
        q += cp.getCriteria(vraag);
        
      //  vraag.getStuurgegevens().
        // Stel query samen
        return q;
    }
    
    private List<Map<String,Object>> getResults(String query, VraagBericht vraag){
        List<Map<String,Object>> results = null;
        return results;
    }
    
    private void sort(List<Map<String,Object>> results, VraagBericht vraag){
        
    }
    
    private List<Object> createResults(List<Map<String,Object>> resultsMap, VraagBericht vraag){
        List<Object> results = null;
        return results;
    }
    
    private Body createBody(List<Object> objs){
        Body b = new Body();
        
        return b;
    }
}
