/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204.util;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class DiagnosticsServlet implements Servlet {

    private static final Log LOG = LogFactory.getLog(DiagnosticsServlet.class);
    private static DataSource stagingDs = null;
    private static JAXBContext jaxbContext;
    private static final String JNDI_NAME = "java:comp/env";
    private static final String JDBC_NAME_STAGING = "jdbc/brmo/staging";

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            LOG.info("Starten StUF BG 205 web applicatie.");
			/*
            // lookup db connectie en log info
            DataSource rsgb = BrkInfo.getDataSourceRsgb();
            DatabaseMetaData metadata = rsgb.getConnection().getMetaData();
            LOG.info(String.format("\nDatabase en driver informatie\n\n  Database product: %s\n  Database version: %s\n  Database major:   %s\n  Database minor:   %s\n\n  DBdriver product: %s\n  DBdriver version: %s\n  DBdriver major:   %s\n  DBdriver minor:   %s",
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion().replace('\n', ' '),
                    metadata.getDatabaseMajorVersion(),
                    metadata.getDatabaseMinorVersion(),
                    metadata.getDriverName(),
                    metadata.getDriverVersion(),
                    metadata.getDriverMajorVersion(),
                    metadata.getDriverMinorVersion()
            ));

            // foute oracle drivers loggen
            if (metadata.getDriverName().startsWith("Oracle")) {
                if (metadata.getDriverVersion().startsWith("12.1.0.1")) {
                    LOG.error("De geïnstalleerde JDBC driver heeft bekende problemen met prepared statements, de applicatie zal niet goed werken. Issue #322");
                    LOG.info("zie: https://github.com/B3Partners/brmo/wiki/Welke-jdbc-driver voor meer informatie");
                }

                // 11.2.0.3
                if (metadata.getDriverVersion().startsWith("11.2.0.3")) {
                    LOG.error("De geïnstalleerde JDBC driver heeft bekende problemen met type conversies, de applicatie zal mogelijk niet goed werken. Issue #322");
                    LOG.info("zie: https://github.com/B3Partners/brmo/wiki/Welke-jdbc-driver voor meer informatie");
                }
            }
            */
        } catch (Exception ex) {
            LOG.error(ex);
        }
        
    }

    @Override
    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getServletInfo() {
        return "Diagnostics info logging servlet";
    }

    @Override
    public void destroy() {
        // nothing
    }
    
    public static JAXBContext getStufJaxbContext() throws JAXBException{
        if(jaxbContext == null){
            jaxbContext = JAXBContext.newInstance("nl.egem.stuf.sector.bg._0204:nl.egem.stuf.stuf0204");
        }
        return jaxbContext;
    }

    public static DataSource getDataSourceStaging() throws Exception {
        if (stagingDs != null) {
            return stagingDs;
        }
        try {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup(JNDI_NAME);
            stagingDs = (DataSource) xmlContext.lookup(JDBC_NAME_STAGING);
        } catch (NamingException ex) {
            LOG.error("Fout verbinden naar rsgb db. ", ex);
            throw ex;
        }

        return stagingDs;
    }
}
