package nl.b3p.brmo.soap.eigendom;

import java.util.Date;
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
        
        String id = request.getIdentificatie();
        Integer maxNum = request.getMaxAantalResultaten();
        if (maxNum > 100) {
            throw new EigendomMutatieException("Ongeldige invoer", "Er kunnen maximaal 100 objecten opgevraagd worden");
        }
        
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
        em.setIdentificatienummer(id);
        
        eir.getEigendomMutatie().add(em);
        return eir;
    }

    @WebMethod(operationName = "getMutatieList")
    public MutatieListResponse getMutatieList(@WebParam(name = "request") MutatieListRequest request) throws EigendomMutatieException {

        Date fromDate = null;
        XMLGregorianCalendar xmlFromDate = request.getFromDate();
        if (xmlFromDate!=null) {
            fromDate = xmlFromDate.toGregorianCalendar().getTime();
        }
        Date toDate = null;
        XMLGregorianCalendar xmlToDate = request.getToDate();
        if (xmlToDate!=null) {
            toDate = xmlToDate.toGregorianCalendar().getTime();
        }

//        Elk BAG-object heeft een unieke identificatie.
//            * Woonplaats – NNNN
//            Woonplaatsen hebben een unieke woonplaatscode van vier cijfers.
//                    
//            * Overige BAG-objecten – GGGGTTNNNNNNNNNN
//            Alle overige objecten hebben een unieke identificatie van dit formaat:
//            * GGGG = gemeentecode
//            De gemeentecode van de gemeente die het object als eerste heeft opgevoerd. Bij een
//            gemeentelijke herindeling blijft de identificatie van de objecten binnen die gemeente ongewijzigd.
//            * TT = objecttypecode
//                01 verblijfsobject
//                02 ligplaats
//                03 standplaats
//                10 pand
//                20 nummeraanduiding
//                30 openbare ruimte
//                40 woonplaats
//            * NNNNNNNNNN = binnen een gemeente uniek tiencijferig nummer

        String objectprefix = request.getObjectprefix();
        if (objectprefix.equalsIgnoreCase("VBO:007")) {
            throw new EigendomMutatieException("Ongeldige invoer", "VBO:007 is geen geldige waarde!");
        }
        
        
        
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
        
        mlr.getMutatieList().add("NL.KAD.OnroerendeZaak:54610225570000");
        mlr.getMutatieList().add("VBO:0355010000819550");
        
        return mlr;
    }

}
