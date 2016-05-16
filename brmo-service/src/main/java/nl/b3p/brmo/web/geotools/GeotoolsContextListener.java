/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.web.geotools;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.geotools.factory.GeoTools;
import org.geotools.util.logging.Logging;

/**
 *
 * @author mprins
 */
public class GeotoolsContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            GeoTools.init();
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException | IllegalArgumentException ex) {
            // ignore
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        org.geotools.util.WeakCollectionCleaner.DEFAULT.exit();
    }

}
