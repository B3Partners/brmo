/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.stufbg204.util.StUFbg204Util;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import nl.egem.stuf.sector.bg._0204.PRSAntwoord;
import nl.egem.stuf.sector.bg._0204.StUFFout;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import nl.egem.stuf.stuf0204.Verwerkingssoort;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
@WebService(
        serviceName = "StUFBGSynchroon",
        portName = "StUFBGSynchronePort",
        endpointInterface = "nl.egem.stuf.sector.bg._0204.StUFBGSynchroonPortType",
        targetNamespace = "http://www.egem.nl/StUF/sector/bg/0204",
        wsdlLocation = "WEB-INF/wsdl/bg0204.wsdl"
)
@HandlerChain(file = "/handler-chain.xml")
public class StUFBGsynchroon {

    private static final Log LOG = LogFactory.getLog(StUFBGsynchroon.class);

    public SynchroonAntwoordBericht beantwoordSynchroneVraag(VraagBericht vraag) throws StUFFout {
        try {
            LOG.debug("Er is vraag ontvangen van soort: " + vraag.getStuurgegevens().getBerichtsoort());
            SynchroonAntwoordBericht antw = new SynchroonAntwoordBericht();
            antw.setStuurgegevens(StUFbg204Util.maakStuurgegevens(vraag.getStuurgegevens()));
            Body b = process(vraag);
            antw.setBody(b);
            try {
                String vraagXml = getXml(vraag);
                LOG.debug("Vraagbericht: " + vraagXml);
                String antwoordXml = getXml(antw);
                LOG.debug("Antwoordbericht: " + antwoordXml);
            } catch (JAXBException ex) {
                LOG.debug("Cannot output vraag/antwoord:",ex);
            }
            return antw;
        } catch (SQLException | BrmoException e) {
            FoutBericht fout = StUFbg204Util.maakFout("StUF011", e);
            throw new StUFFout("Not implemented yet.", fout, e);
        } catch (StUFFout e) {
            throw e;
        }
    }

    private Body process(VraagBericht vraag) throws BrmoException, SQLException, StUFFout {
        // interpreteer vraag
        String q = null;
        DataSource d = ConfigUtil.getDataSourceRsgb();
        Connection c = d.getConnection();
        
        try {
            q = createQuery(vraag,c);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            LOG.error("Cannot parse query: ", e);
            FoutBericht fout = StUFbg204Util.maakFout("StUF011", e);
            throw new StUFFout("Cannot parse query: ", fout, e);
        }
        // haal resultaten op
        List<Map<String, Object>> results = getResults(q, vraag,c);
        DbUtils.closeQuietly(c);
        // Sorteer resultaten
        sort(results, vraag);
        // maak entities adhv gevraagde elementen
        Body b = createResults(results, vraag);
        return b;
    }

    private String createQuery(VraagBericht vraag, Connection c) throws IllegalArgumentException, StUFFout,UnsupportedOperationException {
        Stuurgegevens sg = vraag.getStuurgegevens();
        String q = "select * from ";
        String entiteitType = sg.getEntiteittype();
        nl.egem.stuf.sector.bg._0204.VraagBericht.Body b = vraag.getBody();
        // Haal op wat de gevraagde entiteit is
        // haal de rsgb tabellen op
        switch (entiteitType) {
            case "PRS": {
                q += "ingeschr_nat_prs inp inner join subject s on inp.sc_identif = s.identif inner join nat_prs np on np.sc_identif = s.identif ";
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        // Haal op wat het criterium is
        CriteriaParser cp = new CriteriaParser();

        // Stel query samen
        String whereClause = cp.getCriteria(vraag);
        if (whereClause != null) {
            q += whereClause;
        }

        String order = getOrderString(vraag);
        q += " " + order;

        if (vraag.getStuurgegevens().getVraag().getMaximumAantal() != null) {
            GeometryJdbcConverter converter = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            q = converter.buildLimitSql(new StringBuilder(q), vraag.getStuurgegevens().getVraag().getMaximumAantal().intValue()).toString();
        }
        return q;
    }

    private String getOrderString(VraagBericht vraag) throws StUFFout {
        String sort = "";
        BigInteger sortering = vraag.getStuurgegevens().getVraag().getSortering();
        if (sortering != null) {
            if (sortering.compareTo(new BigInteger("1")) == -1 || sortering.compareTo(new BigInteger("9")) == 1) {
                FoutBericht fout = StUFbg204Util.maakFout("StUF004");
                throw new StUFFout("Sortering niet ondersteund: " + sortering, fout);
            } else {
                sort = "ORDER BY ";
                switch (sortering.toString()) {
                    case "1":
                        sort += "nm_geslachtsnaam,nm_voorvoegsel_geslachtsnaam, na_voorletters_aanschrijving";
                        break;
                    case "6":
                        sort += "gb_geboortedatum,nm_geslachtsnaam,nm_voorvoegsel_geslachtsnaam, na_voorletters_aanschrijving";
                        break;
                    case "7":
                        sort += "bsn";
                        break;
                    case "8":
                        sort += "a_nummer";
                        break;
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "9":
                        // 2,3,4,5,9 worden via post processing gedaan
                        break;
                    default:
                        FoutBericht fout = StUFbg204Util.maakFout("StUF004");
                        throw new StUFFout("Sortering niet ondersteund: " + sortering, fout);
                }
            }
        } else {
            sort = "ORDER BY identif";
        }
        return sort;
    }

    private void sort(List<Map<String, Object>> results, VraagBericht vraag) throws StUFFout {
        /*
            01: Geslachtsnaam, voorvoegsel geslachtsnaam, voorletters
            02: Postcode, huisnummer, huisletter van het verblijfsadres
            03: Straatnaam, huisnummer, huisletter van het verblijfsadres
            04: Postcode, huisnummer, huisletter van het inschrijvingsadres
            05: Straatnaam, huisnummer, huisletter van het inschrijvingsadres
            06: Geboortedatum, geslachtsnaam, voorvoegsel geslachtsnaam, voorletters
            07: SoFi-nummer
            08: A-nummer
            09: Subjectnummer AKR Omdat een persoon meerdere subjectnummers AKR kan hebben kan een persoon bij deze sortering meerdere keren in het bericht voorkomen.
         */
        BigInteger sortering = vraag.getStuurgegevens().getVraag().getSortering();
        if (sortering != null) {

        }
    }

    private List<Map<String, Object>> getResults(String query, VraagBericht vraag, Connection c) throws BrmoException, SQLException {
        List<Map<String, Object>> results;

        MapListHandler mlh = new MapListHandler();
        QueryRunner qr = new QueryRunner();
        results = qr.query(c, query, mlh);
        return results;
       
    }

    private Body createResults(List<Map<String, Object>> resultsMap, VraagBericht vraag) {
        Body b = new Body();
        String entiteitType = vraag.getStuurgegevens().getEntiteittype();
        switch (entiteitType) {
            case "PRS": {
                for (Map<String, Object> obj : resultsMap) {
                    PRSAntwoord prs = AntwoordBodyFactory.createPersoon(obj, vraag.getBody().getPRS().get(2));
                    prs.setSoortEntiteit("F");
                    String sleutel = obj.get("sc_identif").toString();
                    prs.setSleutelVerzendend(sleutel);
                    prs.setVerwerkingssoort(Verwerkingssoort.I);
                    b.getPRS().add(prs);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        return b;
    }
    
    private String getXml(Object o ) throws JAXBException{
        Marshaller jaxbMarshaller = StUFbg204Util.getStufJaxbContext().createMarshaller();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(o, baos);
        return baos.toString();
    }
}
