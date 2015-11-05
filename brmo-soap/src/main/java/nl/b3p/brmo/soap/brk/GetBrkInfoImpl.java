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

        Map<String, Object> searchContext = createSearchContext(request);
        if (searchContext.isEmpty()) {
            throw new BrkInfoException("Request not valid", "minimaal één zoekterm vereist!");
        }
        
        ArrayList<Integer> ids = null;
        try {
            ids = BrkInfo.findKozIDs(searchContext);
        } catch (Exception ex) {
            throw new BrkInfoException("Database reported errors", ex.getLocalizedMessage());
        }

        if (ids==null || ids.isEmpty()) {
            throw new BrkInfoException("Database reported", "Geen resultaten");
        }
        
        if (ids.size() > (Integer)searchContext.get(BrkInfo.MAXAANTALRESULTATEN)) {
            throw new BrkInfoException("Database reported", "Meer resultaten dan toegestaan, pas uw voorwaarden aan!");
        }
        
        BrkInfoResponse result = new BrkInfoResponse();
        result.setBevatGevoeligeInfo(true);

        for (Integer id : ids) {
            KadOnrndZkInfoResponse koz = new KadOnrndZkInfoResponse();
            koz.setIdentificatie(id.toString());
            result.addKadOnrndZk(koz);
        }

        return result;
    }

    private Map<String, Object> createSearchContext(BrkInfoRequest request) {
        Map<String, Object> searchContext = new HashMap<String, Object>();
        if (request == null) {
            return searchContext;
        }

        if (request.getSubjectNaam() != null) {
            searchContext.put(BrkInfo.SUBJECTNAAM, request.getSubjectNaam());
        }
        if (request.getZoekgebied() != null) {
            searchContext.put(BrkInfo.ZOEKGEBIED, request.getZoekgebied());
        }

        KadOnrndZkInfoRequest koz = request.getKadOnrndZk();
        if (koz != null) {

            if (koz.getIdentificatie() != null) {
                searchContext.put(BrkInfo.IDENTIFICATIE, koz.getIdentificatie());
            }
            if (koz.getAppReVolgnummer() != null) {
                searchContext.put(BrkInfo.APPREVOLGNUMMER, koz.getAppReVolgnummer());
            }
            if (koz.getGemeentecode() != null) {
                searchContext.put(BrkInfo.GEMEENTECODE, koz.getGemeentecode());
            }
            if (koz.getPerceelnummer() != null) {
                searchContext.put(BrkInfo.PERCEELNUMMER, koz.getPerceelnummer());
            }
            if (koz.getSectie() != null) {
                searchContext.put(BrkInfo.SECTIE, koz.getSectie());
            }
        }
        PerceelAdresInfoRequest pa = request.getPerceelAdres();
        if (pa != null) {
            if (pa.getGemeenteNaam() != null) {
                searchContext.put(BrkInfo.GEMEENTENAAM, pa.getGemeenteNaam());
            }
            if (pa.getHuisnummer() != null) {
                searchContext.put(BrkInfo.HUISNUMMER, pa.getHuisnummer());
            }
            if (pa.getPostcode() != null) {
                searchContext.put(BrkInfo.POSTCODE, pa.getPostcode());
            }
            if (pa.getStraatNaam() != null) {
                searchContext.put(BrkInfo.STRAATNAAM, pa.getStraatNaam());
            }
        }

        // parameters alleen toevoegen als searchContext niet leeg is
        // verder wordt test op leeg gebruikt om te zien of er gezocht moet worden
        if (!searchContext.isEmpty()) {
            if (request.getMaxAantalResultaten() != null) {
                searchContext.put(BrkInfo.MAXAANTALRESULTATEN, request.getMaxAantalResultaten());
            }
            if (request.getAdressenToevoegen() != null) {
                searchContext.put(BrkInfo.ADRESSENTOEVOEGEN, request.getAdressenToevoegen());
            }
            if (request.getGevoeligeInfoOphalen() != null) {
                searchContext.put(BrkInfo.GEVOELIGEINFOOPHALEN, request.getGevoeligeInfoOphalen());
            }
            if (request.getSubjectsToevoegen() != null) {
                searchContext.put(BrkInfo.SUBJECTSTOEVOEGEN, request.getSubjectsToevoegen());
            }
        }

        return searchContext;
    }

}
