package nl.b3p.brmo.soap.eigendom;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Chris
 */
@WebService(
        serviceName = "EigendomMutatieService",
        targetNamespace = "http://brmo.b3p.nl/brmo/1.0/eigendom",
        wsdlLocation = "WEB-INF/wsdl/EigendomMutatie.wsdl"
)
public class EigendomMutatieService {

    @WebMethod(operationName = "getEigendomMutaties")
    public EigendomMutatieResponse getEigendomMutaties(@WebParam(name = "request") EigendomMutatieRequest request) throws EigendomMutatieException {
        EigendomMutatieResponse eir = new EigendomMutatieResponse();
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now
                    = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
           eir.setTimestamp(now);

        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(EigendomMutatieService.class.getName()).log(Level.SEVERE, null, ex);
        }
        EigendomMutatie em = EigendomMutatie.createDummy();
        
        eir.getEigendomMutatie().add(em);
        return eir;
    }

    @WebMethod(operationName = "getMutatieList")
    public MutatieListResponse getMutatieList(@WebParam(name = "request") MutatieListRequest request) throws EigendomMutatieException {
        MutatieListResponse mlr = new MutatieListResponse();
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now
                    = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
           mlr.setTimestamp(now);

        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(EigendomMutatieService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        mlr.getMutatieList().add("0001");
        mlr.getMutatieList().add("0002");
        mlr.getMutatieList().add("0003");
        mlr.getMutatieList().add("0004");
        
        return mlr;
    }

}
