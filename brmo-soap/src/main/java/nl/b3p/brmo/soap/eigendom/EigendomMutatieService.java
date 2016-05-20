package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Chris
 */


@WebService(serviceName = "EigendomMutatieService")
public class EigendomMutatieService {

    @WebMethod(operationName = "getEigendomMutaties")
    public EigendomMutatieResponse getEigendomMutaties(@WebParam(name = "request") EigendomMutatieRequest request) throws EigendomMutatieException {
        EigendomMutatieResponse eir = new EigendomMutatieResponse();
        eir.setTimestamp(new Date());
        EigendomMutatie kzr = new EigendomMutatie();
        kzr.setIdentificatie(request.getIdentificatie().toString());
        List<EigendomMutatie> kzrl = new ArrayList<>();
        eir.setEigendomMutatie(kzrl);
        
        return eir;
    }
    
    @WebMethod(operationName = "getMutatieList")
    public List getMutatieList(@WebParam(name = "fromdate") Date fromDate, @WebParam(name = "toDate") Date toDate) throws EigendomMutatieException {
        List<String> l = new ArrayList<>();
        l.add("Period: " + fromDate.toString() + " to " + toDate.toString());
        return l;
    }
    
    
}
