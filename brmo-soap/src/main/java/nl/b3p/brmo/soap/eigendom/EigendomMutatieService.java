package nl.b3p.brmo.soap.eigendom;

import java.util.Map;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import nl.b3p.brmo.soap.db.EigendomInfo;

/**
 *
 * @author Chris
 */
@WebService(
        serviceName = "EigendomMutatieService",
        targetNamespace = "http://brmo.b3p.nl/brmo/1.0/eigendom",
        wsdlLocation = "WEB-INF/wsdl/EigendomMutatie.wsdl"
)
@HandlerChain(file = "/handler-chain.xml")
public class EigendomMutatieService {

    @WebMethod(operationName = "getEigendomMutaties")
    public EigendomMutatieResponse getEigendomMutaties(@WebParam(name = "request") EigendomMutatieRequest request) throws EigendomMutatieException {
        Map<String, Object> eigendomMutatieContext = EigendomInfo.createEigendomMutatieContext(request);
        return EigendomInfo.createEigendomMutatieResponse(eigendomMutatieContext);
    }

    @WebMethod(operationName = "getMutatieList")
    public MutatieListResponse getMutatieList(@WebParam(name = "request") MutatieListRequest request) throws EigendomMutatieException {
        Map<String, Object> mutatieListContext = EigendomInfo.createMutatieListContext(request);
        return EigendomInfo.createMutatieListResponse(mutatieListContext);
    }

}
