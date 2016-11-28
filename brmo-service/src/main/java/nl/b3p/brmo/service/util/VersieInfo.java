/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.util;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * log versie informatie van de gebruikte schema's. Maakt gebruik van
 * {@link ConfigUtil} om de datasources op te halen.
 *
 * @author mprins
 * @since 1.4.1
 */
public class VersieInfo implements Servlet {

    private static final Log log = LogFactory.getLog(VersieInfo.class);



    @Override
    public void init(ServletConfig config) throws ServletException {
        BrmoFramework brmo = null;
        String appVersie = "mislukt";
        final Properties props = new Properties();
        try {
            props.load(VersieInfo.class.getClassLoader().getResourceAsStream("git.properties"));
            appVersie = props.getProperty("builddetails.build.version", "onbekend");
        } catch (IOException ex) {
            log.warn("Ophalen BRMO applicatie versie informatie is mislukt.", ex);
        }

        try {
            brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), ConfigUtil.getDataSourceRsgb(), ConfigUtil.getDataSourceRsgbBgt());
            log.info("BRMO versie informatie:");
            log.info("  staging schema versie is:  " + brmo.getStagingVersion());
            log.info("  rsgb schema versie is:     " + brmo.getRsgbVersion());
            log.info("  rsgbbgt schema versie is:  " + brmo.getRsgbBgtVersion());
            log.info("  brmo applicatie versie is: " + appVersie);
            // TODO check voor passende versies
        } catch (BrmoException ex) {
            log.warn("Ophalen BRMO versie informatie is mislukt.", ex);
        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getServletInfo() {
        return "Versie info logging servlet";
    }

    @Override
    public void destroy() {
        // leeg
    }
}
