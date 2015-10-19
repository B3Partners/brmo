package nl.b3p.brmo.service.util;

import java.io.IOException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigUtil implements Servlet {

    private static final Log log = LogFactory.getLog(ConfigUtil.class);

    private static final String JNDI_NAME = "java:comp/env";
    private static final String JDBC_NAME_STAGING = "jdbc/brmo/staging";
    private static final String JDBC_NAME_RSGB = "jdbc/brmo/rsgb";

    private static DataSource datasourceStaging = null;
    private static DataSource datasourceRsgb = null;
    
    public static Integer MAX_UPLOAD_SIZE;
    public static String TEMP_FOLDER;

    public static boolean PMD_KNOWN_BROKEN = false;

    public void init(ServletConfig config) throws ServletException {
        String tempSize = config.getInitParameter("max_upload_size");
        
        if (tempSize != null && !tempSize.isEmpty()) {          
            MAX_UPLOAD_SIZE = new Integer(tempSize) * 1024;
        } else {
            MAX_UPLOAD_SIZE = 500 * 1024;
        }
        
        String tempFolder = config.getInitParameter("temp_folder");
        if (tempFolder != null && !tempFolder.isEmpty()) {          
            TEMP_FOLDER = tempFolder;
        } else {
            TEMP_FOLDER = "/tmp";
        }

        PMD_KNOWN_BROKEN = "true".equals(config.getServletContext().getInitParameter("database.pmdKnownBroken"));
    }

    public static DataSource getDataSourceStaging() throws BrmoException {
        DataSource ds = null;
        try {
            if (datasourceStaging == null) {
                InitialContext ic = new InitialContext();
                Context xmlContext = (Context) ic.lookup(JNDI_NAME);
                ds = (DataSource) xmlContext.lookup(JDBC_NAME_STAGING);
            }
        } catch (Exception ex) {
            log.error("Fout verbinden naar staging db. ", ex);
            throw new BrmoException(ex);
        }

        return ds;
    }
    
    public static DataSource getDataSourceRsgb() throws BrmoException {
        DataSource ds = null;
        try {
            if (datasourceRsgb == null) {
                InitialContext ic = new InitialContext();
                Context xmlContext = (Context) ic.lookup(JNDI_NAME);
                ds = (DataSource) xmlContext.lookup(JDBC_NAME_RSGB);
            }
        } catch (Exception ex) {
            log.error("Fout verbinden naar rsgb db. ", ex);
            throw new BrmoException(ex);
        }

        return ds;
    }

    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public void service(ServletRequest sr, ServletResponse sr1) throws ServletException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public String getServletInfo() {
        return "Servlet used for cofngiuration parameters in web.xml";
    }

    public void destroy() {
        // nothing
    }
}
