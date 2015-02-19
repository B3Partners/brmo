/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.FoutType;
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

            log.info(String.format("AfleverRequest (type %s) ontvangen met kenmerk %s.",
                    verzoek.getBerichtsoort(), verzoek.getBerichtkenmerk()));
            
            if (verzoek.getBerichtsoort().equalsIgnoreCase("BAG")) {
                BAGAfleverRequestHandler h = new BAGAfleverRequestHandler();
                h.handle(verzoek, antwoord);
                // }else if( TODO andere berichten...){
            } else {
                // foutmelding genereren
                String msg = String.format("Er is een niet ondersteunde AfleverRequest (type %s) gestuurd.", verzoek.getBerichtsoort());
                log.warn(msg);
                FoutType t = new FoutType();
                t.setFoutbeschrijving(msg);
                t.setFoutcode(AfleverRequestHandling.STATUSCODE.ERROR.toString());
                antwoord.setStatusFoutcode(t);
            }

            // antwoord terug sturen
            Marshaller m = jc.createMarshaller();
            m.marshal(antwoord, response.getOutputStream());
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();

        } catch (JAXBException ex) {
            log.error(ex);
            throw new ServletException(ex);
        }
    }

}
