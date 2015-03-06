/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private EbMSBerichtenProcessor ebMSBerichtenProcessor;

    @Override
    public void init() throws ServletException {
        ebMSBerichtenProcessor = new EbMSBerichtenProcessor(this.getInitParameter(/* TODO */"opslagDir"));
    }

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

        this.ebMSBerichtenProcessor.processAfleverRequest(request.getInputStream(), response.getOutputStream());

        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();

    }

}
