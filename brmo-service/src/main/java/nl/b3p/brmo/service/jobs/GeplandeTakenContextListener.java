/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.QUARTZ_FACTORY_KEY;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.SCHEDULER_NAME;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author mprins
 */
public class GeplandeTakenContextListener implements ServletContextListener {

    /**
     * Doet niets, setup van de StdSchedulerFactory doen we in een servlet
     * vanwege initialisatie van stripes die eerst gebeurt in het stripes
     * filter.
     *
     * @param sce de servlet context
     * @see GeplandeTakenInit
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            StdSchedulerFactory f = (StdSchedulerFactory) sce.getServletContext().
                    getAttribute(QUARTZ_FACTORY_KEY);
            sce.getServletContext().removeAttribute(QUARTZ_FACTORY_KEY);
            if (f != null) {
                f.getScheduler(SCHEDULER_NAME).shutdown();
            }
        } catch (Exception ex) {
            // sce.getServletContext().log(ex);
        }
    }
}
