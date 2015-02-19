/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.BerichtInhoudType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ontvangt aflever berichten/verzoeken.
 *
 * @see nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest
 * @see nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse
 * @author Mark Prins <mark@b3partners.nl>
 */
public class DigileveringEbMSAfleverService extends HttpServlet {

    private static final Log log = LogFactory.getLog(DigileveringEbMSAfleverService.class);

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AfleverResponse antwoord = new AfleverResponse();
        AfleverRequest verzoek;
        // AfleverFault fout;

        try {
            InputStream in = request.getInputStream();
            JAXBContext jc = JAXBContext.newInstance("nl.b3p.brmo.digipoort.koppelvlakservices._1_2");
            Unmarshaller u = jc.createUnmarshaller();
            verzoek = (AfleverRequest) u.unmarshal(in);

            handleRequest(verzoek, antwoord);

            // antwoord terug sturen
            Marshaller m = jc.createMarshaller();
            m.marshal(antwoord, response.getOutputStream());

        } catch (JAXBException ex) {
            log.error(ex);
            throw new ServletException(ex);
        }
    }

    /**
     * afhandelen van het aflever verzoek.
     *
     * @param verzoek
     * @param antwoord
     */
    private void handleRequest(AfleverRequest verzoek, AfleverResponse antwoord) {
        // inhoud ophalen
        antwoord.setTijdstempelAfgeleverd(XMLUtil.getNow());

        BerichtInhoudType t = verzoek.getBerichtInhoud();
        String bNaam = t.getBestandsnaam();
        String mType = t.getMimeType();
        byte[] inhoud = t.getInhoud();

        // TODO "iets" doen met bericht in het verzoek
        if (mType.equalsIgnoreCase("application/xml")) {

        } else if (mType.equalsIgnoreCase("application/base64")) {
            try {
                inhoud = DatatypeConverter.parseBase64Binary(new String(inhoud, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                // TODO
            }
        }

        // TODO antwoord aanpassen...
        antwoord.setKenmerk(verzoek.getKenmerk());
        antwoord.setBerichtsoort(verzoek.getBerichtsoort());
        antwoord.setBerichtkenmerk(verzoek.getBerichtkenmerk());
        antwoord.setTijdstempelStatus(XMLUtil.getNow());
    }

}
