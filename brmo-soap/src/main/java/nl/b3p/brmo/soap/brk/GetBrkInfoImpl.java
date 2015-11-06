package nl.b3p.brmo.soap.brk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import nl.b3p.brmo.soap.db.BrkInfo;

@WebService
public class GetBrkInfoImpl {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getBrkInfo")
    @RequestWrapper(className = "nl.b3p.brmo.soap.brk.getBrkInfo")
    @ResponseWrapper(className = "nl.b3p.brmo.soap.brk.getBrkInfoResponse")
    public BrkInfoResponse getBrkInfo(@WebParam(name = "request") BrkInfoRequest request) throws BrkInfoException {

        Map<String, Object> searchContext = BrkInfo.createSearchContext(request);
        if (searchContext.isEmpty()) {
            throw new BrkInfoException("Request not valid", "minimaal één zoekterm vereist!");
        }
        
        ArrayList<Long> ids = null;
        try {
            ids = BrkInfo.findKozIDs(searchContext);
        } catch (Exception ex) {
            throw new BrkInfoException("Database reported errors", ex.getLocalizedMessage());
        }

        if (ids==null || ids.isEmpty()) {
            throw new BrkInfoException("Database reported", "Geen resultaten");
        }
        
        if (ids.size() > (Integer)searchContext.get(BrkInfo.MAXAANTALRESULTATEN)) {
            throw new BrkInfoException("Database reported", 
                    "Meer resultaten dan toegestaan: "
                            +(Integer)searchContext.get(BrkInfo.MAXAANTALRESULTATEN)
                            +", pas uw voorwaarden aan!");
        }
        
        BrkInfoResponse result = null;
        try {
            result = BrkInfo.createResponse(ids, searchContext);
        } catch (Exception ex) {
            throw new BrkInfoException("Database reported errors", ex.getLocalizedMessage());
        }
        
        return result;
    }

}
