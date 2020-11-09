package nl.b3p.brmo.soap.brk;

import java.util.ArrayList;
import java.util.Map;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;
import nl.b3p.brmo.soap.db.BrkInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebService(
        targetNamespace = "http://brmo.b3p.nl/brk/1.0/soap-brk",
        wsdlLocation = "WEB-INF/wsdl/brkinfo.wsdl"
)
@HandlerChain(file = "/handler-chain.xml")
public class GetBrkInfoImpl {
    private static final Log LOG = LogFactory.getLog(GetBrkInfoImpl.class);
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
            LOG.error(ex);
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
            LOG.error(ex);
            throw new BrkInfoException("Database reported errors", ex.getLocalizedMessage());
        }
        
        return result;
    }

}
