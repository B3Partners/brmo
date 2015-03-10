/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.SCHEDULER_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class GeplandeTakenContextListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(GeplandeTakenContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // setup doen we in een servlet vanwege initialisatie van stripes
        // die eerst gebeurt in het stripes filter
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Scheduler scheduler = (Scheduler) sce.getServletContext().getAttribute(SCHEDULER_NAME);
        try {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        } catch (SchedulerException ex) {
            log.error(ex);
        }
        sce.getServletContext().removeAttribute(SCHEDULER_NAME);
    }
}
