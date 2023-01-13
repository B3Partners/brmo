/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.NHR_QUARTZ_FACTORY_KEY;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.NHR_SCHEDULER_NAME;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.QUARTZ_FACTORY_KEY;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.SCHEDULER_NAME;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** @author mprins */
public class GeplandeTakenContextListener implements ServletContextListener {

    /**
     * Doet niets, setup van de StdSchedulerFactory doen we in een servlet vanwege initialisatie van
     * stripes die eerst gebeurt in het stripes filter.
     *
     * @param sce de servlet context
     * @see GeplandeTakenInit
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {}

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            StdSchedulerFactory f =
                    (StdSchedulerFactory) sce.getServletContext().getAttribute(QUARTZ_FACTORY_KEY);
            sce.getServletContext().removeAttribute(QUARTZ_FACTORY_KEY);
            if (f != null) {
                Scheduler s = f.getScheduler(SCHEDULER_NAME);
                if (s != null && !s.isShutdown()) {
                    s.shutdown(true);
                }
            }

            f = (StdSchedulerFactory) sce.getServletContext().getAttribute(NHR_QUARTZ_FACTORY_KEY);
            sce.getServletContext().removeAttribute(NHR_QUARTZ_FACTORY_KEY);
            if (f != null) {
                Scheduler s = f.getScheduler(NHR_SCHEDULER_NAME);
                if (s != null && !s.isShutdown()) {
                    s.shutdown(false);
                }
            }
        } catch (Exception ex) {
            sce.getServletContext().log("Stoppen van schedulers mislukt", ex);
        }
    }
}
