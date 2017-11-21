/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * log versie informatie van de gebruikte schema's. Maakt gebruik van
 * {@link ConfigUtil} om de datasources op te halen. Voeg deze servlet toe aan
 * de web.xml van jouw brmo webapp met een startup parameter hoger dan de
 * {@link ConfigUtil} servlet. Voorbeeld:
 * <pre>
 * &lt;servlet&gt;
 *      &lt;servlet-name&gt;VersieInfo&lt;/servlet-name&gt;
 *      &lt;description&gt;versie info controle&lt;/description&gt;
 *      &lt;servlet-class&gt;nl.b3p.brmo.service.util.VersieInfo&lt;/servlet-class&gt;
 *      &lt;load-on-startup&gt;4&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * </pre>
 *
 * @author mprins
 * @since 1.4.1
 */
public class VersieInfo implements Servlet {

    private static final Log LOG = LogFactory.getLog(VersieInfo.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        BrmoFramework brmo = null;
        String appVersie = "mislukt";
        final Properties props = new Properties();
        try {
            props.load(VersieInfo.class.getClassLoader().getResourceAsStream("git.properties"));
            appVersie = props.getProperty("builddetails.build.version", "onbekend");
        } catch (IOException ex) {
            String name = config.getServletContext().getContextPath();
            name = name.startsWith("/") ? name.substring(1).toUpperCase(Locale.ROOT) : "ROOT";
            LOG.warn("Ophalen " + name + " applicatie versie informatie is mislukt.", ex);
        }

        try {
            brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), ConfigUtil.getDataSourceRsgb(), ConfigUtil.getDataSourceRsgbBgt());
            LOG.info("BRMO versie informatie:");
            LOG.info("  staging schema versie is:  " + brmo.getStagingVersion());
            LOG.info("  rsgb schema versie is:     " + brmo.getRsgbVersion());
            LOG.info("  rsgbbgt schema versie is:  " + brmo.getRsgbBgtVersion());
            LOG.info("  brmo applicatie versie is: " + appVersie);
            // TODO check voor passende versies
        } catch (BrmoException ex) {
            LOG.warn("Ophalen BRMO versie informatie is mislukt.", ex);
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
