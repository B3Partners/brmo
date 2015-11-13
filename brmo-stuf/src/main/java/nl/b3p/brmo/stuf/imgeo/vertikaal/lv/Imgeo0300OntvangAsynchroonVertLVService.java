package nl.b3p.brmo.stuf.imgeo.vertikaal.lv;

import javax.jws.WebService;
import nl.egem.stuf.stuf0301.BerichtcodeDi01;
import nl.egem.stuf.stuf0301.OPVStuurgegevensDi01;
import nl.geostandaarden.imgeo._2_1.stuf_imgeo.Fo03;
import nl.egem.stuf.stuf0301.Bv03Bericht.Stuurgegevens;
import nl.egem.stuf.stuf0301.FunctieOPV;
import nl.egem.stuf.stuf0301.Systeem;

/**
 *
 * @author Chris
 */
@WebService(serviceName = "OntvangAsynchroon", portName = "OntvangAsynchroon", endpointInterface = "nl.geostandaarden.imgeo._2_1.stuf_imgeo.OntvangAsynchroon", targetNamespace = "http://www.geostandaarden.nl/imgeo/2.1/stuf-imgeo", wsdlLocation = "WEB-INF/wsdl/imgeo0300_ontvangAsynchroon_vert_LV.wsdl")
public class Imgeo0300OntvangAsynchroonVertLVService {

    public Stuurgegevens opvDi01(OPVStuurgegevensDi01 stuurgegevens, OpvDi01.Ophaalverzoek ophaalverzoek) throws Fo03 {
        BerichtcodeDi01 bc = stuurgegevens.getBerichtcode();
        FunctieOPV f = stuurgegevens.getFunctie();
        Systeem so = stuurgegevens.getOntvanger();
        String ref = stuurgegevens.getReferentienummer();
        String t = stuurgegevens.getTijdstipBericht();
        Systeem sz = stuurgegevens.getZender();
        
        Stuurgegevens sg = new Stuurgegevens();
        sg.setBerichtcode(bc.DI_01.value());
        sg.setCrossRefnummer(ref);
        sg.setOntvanger(sz);
        sg.setReferentienummer("OK");
        sg.setTijdstipBericht(t);
        sg.setZender(so);
        return sg;
    }
    
}
