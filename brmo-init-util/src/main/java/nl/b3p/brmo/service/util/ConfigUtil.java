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
  private static final String JDBC_NAME_RSGBBRK = "jdbc/brmo/rsgbbrk";
  private static final String JDBC_NAME_RSGB_BAG = "jdbc/brmo/rsgbbag";
  private static final String JDBC_NAME_RSGB_BGT = "jdbc/brmo/rsgbbgt";

  private static DataSource datasourceStaging = null;
  private static DataSource datasourceRsgb = null;
  private static DataSource datasourceRsgbBrk = null;
  private static DataSource datasourceRsgbBag = null;
  private static DataSource datasourceRsgbBgt = null;

  public static Long MAX_UPLOAD_SIZE;
  public static String TEMP_FOLDER;

  @Override
  public void init(ServletConfig config) throws ServletException {
    String tempSize = config.getInitParameter("max_upload_size");
    if (tempSize != null && !tempSize.isEmpty()) {
      MAX_UPLOAD_SIZE = Long.parseLong(tempSize) * 1024 * 1024;
    } else {
      MAX_UPLOAD_SIZE = 10000L * 1024 * 1024;
    }

    String tempFolder = config.getInitParameter("temp_folder");
    if (tempFolder != null && !tempFolder.isEmpty()) {
      TEMP_FOLDER = tempFolder;
    } else {
      TEMP_FOLDER = System.getProperty("java.io.tmpdir");
    }
  }

  public static DataSource getDataSourceStaging() throws BrmoException {
    try {
      if (datasourceStaging == null) {
        InitialContext ic = new InitialContext();
        Context xmlContext = (Context) ic.lookup(JNDI_NAME);
        datasourceStaging = (DataSource) xmlContext.lookup(JDBC_NAME_STAGING);
      }
    } catch (Exception ex) {
      log.error("Fout verbinden naar staging db. ", ex);
      throw new BrmoException(ex);
    }

    return datasourceStaging;
  }

  public static DataSource getDataSourceRsgb() throws BrmoException {
    try {
      if (datasourceRsgb == null) {
        InitialContext ic = new InitialContext();
        Context xmlContext = (Context) ic.lookup(JNDI_NAME);
        datasourceRsgb = (DataSource) xmlContext.lookup(JDBC_NAME_RSGB);
      }
    } catch (Exception ex) {
      log.error("Fout verbinden naar rsgb db. ", ex);
      throw new BrmoException(ex);
    }

    return datasourceRsgb;
  }

  public static DataSource getDataSourceRsgbBrk() throws BrmoException {
    try {
      if (datasourceRsgbBrk == null) {
        InitialContext ic = new InitialContext();
        Context xmlContext = (Context) ic.lookup(JNDI_NAME);
        datasourceRsgbBrk = (DataSource) xmlContext.lookup(JDBC_NAME_RSGBBRK);
      }
    } catch (Exception ex) {
      log.error("Fout verbinden naar rsgb db. ", ex);
      throw new BrmoException(ex);
    }

    return datasourceRsgbBrk;
  }

  public static DataSource getDataSourceRsgbBag() throws BrmoException {
    return getDataSourceRsgbBag(true);
  }

  public static DataSource getDataSourceRsgbBag(boolean logErrors) throws BrmoException {
    try {
      if (datasourceRsgbBag == null) {
        InitialContext ic = new InitialContext();
        Context context = (Context) ic.lookup(JNDI_NAME);
        datasourceRsgbBag = (DataSource) context.lookup(JDBC_NAME_RSGB_BAG);
      }
    } catch (Exception ex) {
      if (logErrors) {
        log.error("Fout verbinden naar 'rsgbbag' schema.", ex);
      }
      throw new BrmoException("Fout verbinden naar 'rsgbbag' schema: ", ex);
    }

    return datasourceRsgbBag;
  }

  public static DataSource getDataSourceRsgbBgt() throws BrmoException {
    try {
      if (datasourceRsgbBgt == null) {
        InitialContext ic = new InitialContext();
        Context xmlContext = (Context) ic.lookup(JNDI_NAME);
        datasourceRsgbBgt = (DataSource) xmlContext.lookup(JDBC_NAME_RSGB_BGT);
      }
    } catch (Exception ex) {
      log.error("Fout verbinden naar 'rsgbbgt' schema.", ex);
      throw new BrmoException("Fout verbinden naar 'rsgbbgt' schema.", ex);
    }

    return datasourceRsgbBgt;
  }

  @Override
  public ServletConfig getServletConfig() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void service(ServletRequest sr, ServletResponse sr1) throws ServletException, IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getServletInfo() {
    return "Servlet voor configuratie parameters in web.xml";
  }

  @Override
  public void destroy() {
    // nothing
  }
}
