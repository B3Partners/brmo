package nl.b3p.brmo.soap.brk;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService
public class GetBrkInfoImpl {
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "getBrkInfo")
    @RequestWrapper(className = "nl.b3p.brmo.soap.brk.getBrkInfo")
    @ResponseWrapper(className = "nl.b3p.brmo.soap.brk.getBrkInfoResponse")
    public BrkInfoResponse getBrkInfo(@WebParam(name = "request") BrkInfoRequest request) throws BrkInfoException {
        if (request==null || request.getKadOnrndZk()==null) {
            throw new BrkInfoException("Request not valid or empty", "no detail");
        }
        String sectie = request.getKadOnrndZk().getSectie();
        if (sectie==null) {
            throw new BrkInfoException("Request not valid", "sectie moet ingevuld zijn");
        }
        BrkInfoResponse result = new BrkInfoResponse();
        result.setBevatGevoeligeInfo(true);
        KadOnrndZkInfoResponse koz = new KadOnrndZkInfoResponse();
        koz.setSectie(sectie);
        if (sectie.equalsIgnoreCase("L")) {
            koz.setIdentificatie("10001");
        } else if (sectie.equalsIgnoreCase("P")) {
            koz.setIdentificatie("10002");
        } else {
            koz.setIdentificatie("10003");
        }
        result.setKadOnrndZk(koz);
        return result;
    }

}
