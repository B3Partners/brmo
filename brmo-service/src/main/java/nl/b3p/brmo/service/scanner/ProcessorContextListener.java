/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A manager for a set of ProcesExecutable.
 *
 * @author Mark Prins
 *
 * @deprecated waarschijnlijk niet meer nodig... dit gaat Quartz doen
 */
public class ProcessorContextListener implements ServletContextListener {

    /**
     * read directory scanner configuration from database and initialize
     * required scanners.
     *
     * @param sce used to get a handle on the ServletContext to store the list
     * of DirectoryScanner objects
     *
     * @todo implement
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // read configuration data

            // for each config data record create a scanner
        } catch (Exception e) {
            // TODO where do the errors go? what to do on error?
            sce.getServletContext().log("TODO foutmelding starten automatische processen.", e);
        }
    }

    /**
     * cleanup on context shutdown; stop alle processors.
     *
     * @param sce provides a handle on the ServletContext
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // stop scheduler
        // maak lijst van actieve processen
        // stop ieder proces in de lijst

    }

}
