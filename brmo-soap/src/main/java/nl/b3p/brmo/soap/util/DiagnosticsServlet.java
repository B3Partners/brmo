/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.soap.util;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import nl.b3p.brmo.soap.db.BrkInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class DiagnosticsServlet implements Servlet {

    private static final Log LOG = LogFactory.getLog(DiagnosticsServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {

        try {
            // lookup db connectie en log info
            DataSource rsgb = BrkInfo.getDataSourceRsgb();
            DatabaseMetaData metadata = rsgb.getConnection().getMetaData();
            LOG.info(String.format("Database en driver informatie\nDatabase product: %s\nDatabase version: %s\nDatabase major:   %s\nDatabase minor:   %s\nDatabasedriver product: %s\nDatabasedriver version: %s\nDatabasedriver major:   %s\nDatabasedriver minor:   %s",
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    metadata.getDatabaseMajorVersion(),
                    metadata.getDatabaseMinorVersion(),
                    metadata.getDriverName(),
                    metadata.getDriverVersion(),
                    metadata.getDriverMajorVersion(),
                    metadata.getDriverMinorVersion()
            ));

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

}
