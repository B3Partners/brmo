package nl.b3p.brmo.stufbg204;

import javax.jws.WebService;


/**
 *
 * @author Chris_2
 */


@WebService(serviceName = "StUFBGAsynchroon", portName = "StUFBGAsynchronePort", endpointInterface = "nl.egem.stuf.sector.bg._0204.StUFBGAsynchroonPortType", targetNamespace = "http://www.egem.nl/StUF/sector/bg/0204", wsdlLocation = "WEB-INF/wsdl/bg0204.wsdl")
public class Stufbg204Service {

    public nl.egem.stuf.stuf0204.BevestigingsBericht ontvangKennisgeving(nl.egem.stuf.sector.bg._0204.KennisgevingsBericht kennisgeving) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public nl.egem.stuf.stuf0204.BevestigingsBericht ontvangAsynchroneVraag(nl.egem.stuf.sector.bg._0204.VraagBericht vraag) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public nl.egem.stuf.stuf0204.BevestigingsBericht ontvangAsynchroonAntwoord(nl.egem.stuf.sector.bg._0204.AsynchroonAntwoordBericht asynchroonAntwoord) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public nl.egem.stuf.stuf0204.BevestigingsBericht ontvangFout(nl.egem.stuf.stuf0204.FoutBericht fout) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
}
