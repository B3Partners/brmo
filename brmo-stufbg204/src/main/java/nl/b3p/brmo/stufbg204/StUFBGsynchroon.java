/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.stufbg204.util.StUFbg204Util;
import nl.egem.stuf.sector.bg._0204.PRSAntwoord;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
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
        } catch (SQLException | BrmoException e) {
            FoutBericht fout = StUFbg204Util.maakFout("StUF0011");
            throw new StUFFout("Not implemented yet.", fout, e);
        }catch(StUFFout e){
            throw e;
        }
    }

    private Body process(VraagBericht vraag) throws BrmoException, SQLException, StUFFout {
        // interpreteer vraag
        String q = null;
        try{
            q = createQuery(vraag);
        }catch(IllegalArgumentException e){
            LOG.error("Cannot parse query: ", e);
            FoutBericht fout = StUFbg204Util.maakFout("StUF0011");
            throw new StUFFout("Cannot parse query: ", fout, e);
        }
        // haal resultaten op
        List<Map<String, Object>> results = getResults(q, vraag);
        // Sorteer resultaten
        sort(results, vraag);
        // maak entities adhv gevraagde elementen
        Body b = createResults(results, vraag);
        return b;
    }

    private String createQuery(VraagBericht vraag) throws IllegalArgumentException{
        Stuurgegevens sg = vraag.getStuurgegevens();
        String q = "select * from ";
        String entiteitType = sg.getEntiteittype();
        nl.egem.stuf.sector.bg._0204.VraagBericht.Body b = vraag.getBody();
        // Haal op wat de gevraagde entiteit is
        // haal de rsgb tabellen op
        switch (entiteitType) {
            case "PRS": {
                q += "ingeschr_nat_prs inp inner join subject s on inp.sc_identif = s.identif inner join nat_prs np on np.sc_identif = s.identif ";
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        // Haal op wat het criterium is
        CriteriaParser cp = new CriteriaParser();

        // Stel query samen
        q += "WHERE " + cp.getCriteria(vraag);
        return q;
    }

    private List<Map<String, Object>> getResults(String query, VraagBericht vraag) throws BrmoException, SQLException {
        List<Map<String, Object>> results;
        DataSource d = ConfigUtil.getDataSourceRsgb();
        MapListHandler mlh = new MapListHandler();
        QueryRunner qr = new QueryRunner(d);
        results = qr.query(query, mlh);
        return results;
    }

    private void sort(List<Map<String, Object>> results, VraagBericht vraag) {

    }

    private Body createResults(List<Map<String, Object>> resultsMap, VraagBericht vraag) {
        Body b = new Body();
        String entiteitType = vraag.getStuurgegevens().getEntiteittype();
        switch (entiteitType) {
            case "PRS": {
                for (Map<String,Object> obj : resultsMap) {
                    PRSAntwoord prs = AntwoordBodyFactory.createPersoon(obj, vraag.getBody().getPRS().get(2));
                    b.getPRS().add(prs);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        return b;
    }
}
