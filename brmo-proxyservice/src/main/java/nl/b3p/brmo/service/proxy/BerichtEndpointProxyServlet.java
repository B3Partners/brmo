/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.service.proxy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BerichtEndpointProxyServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(BerichtEndpointProxyServlet.class);

    private static final String PROXY_FOR_URL = "proxy_for_url";
    private URL proxyForUrl;
    private static final String MAX_UPLOAD_SIZE = "max_upload_size";
    private int maxUploadSize;

    @Override
    public void init() throws ServletException {
        try {
            this.maxUploadSize = Integer.parseInt(this.getInitParameter(MAX_UPLOAD_SIZE)) * 1024;
        } catch (NumberFormatException nfe) {
            this.maxUploadSize = 500 * 1024;
            log.warn("De maximale upload size is ingesteld op 500KB (default).");
        }
        try {
            this.proxyForUrl = new URL(this.getInitParameter(PROXY_FOR_URL));
        } catch (MalformedURLException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Handles the HTTP {@code POST} method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getContentLength() > this.maxUploadSize) {
            throw new ServletException("De 'max_upload_size' is overschreden.");
        }
        HttpURLConnection conn = (HttpURLConnection) this.proxyForUrl.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", "" + request.getContentLength());
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        int copied = IOUtils.copy(request.getInputStream(), conn.getOutputStream());
        conn.disconnect();

        log.info(String.format("BRMO response status: %d: %s (%d bytes).",
                conn.getResponseCode(), conn.getResponseMessage(), copied));

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
