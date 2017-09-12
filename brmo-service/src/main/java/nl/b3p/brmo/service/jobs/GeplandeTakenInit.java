/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.text.ParseException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Initialiseer de Quartz scheduler. Vanwege gebruik van de Stripersist entity
 * manager kan dat pas na initialisatie van de Stripes stack en dat gebeurt in
 * een ServletFilter, dus deze servlet moet een auto-start optie krijgen in
 * web.xml waardoor dat na het Filter gebeurt.
 *
 * @author mprins
 */
public class GeplandeTakenInit implements Servlet {

    private static final Log log = LogFactory.getLog(GeplandeTakenInit.class);
    private static Scheduler scheduler;

    public static final String JOBKEY_PREFIX = "proces_";

    public static final String TRIGGERKEY_PREFIX = "trigger_";

    public static final String SCHEDULER_NAME = "BRMOgeplandeTaken";

    public static final String QUARTZ_FACTORY_KEY = "BRMO_qtz_factory";

    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        try {
            this.setupQuartz();
        } catch (SchedulerException ex) {
            log.error(ex);
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getServletInfo() {
        return "Initialiseer Quartz scheduler voor BRMO";
    }

    @Override
    public void destroy() {
        try {
            destroyQuartz();
        } catch (SchedulerException ex) {
            log.warn(ex);
        }
    }

    private void destroyQuartz() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown(true);
        }
    }

    private void setupQuartz() throws SchedulerException {
        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", SCHEDULER_NAME);
        String threadCount = this.getServletConfig().getServletContext().getInitParameter("quartz.threadCount");
        if (threadCount != null && (Integer.parseInt(threadCount) > 1)) {
            log.warn("Instellen van quartz threadcount op niet-default waarde van " + threadCount
                    + " Gebruiker moet zorg dragen dat er geen overlappende transformatie- of GDS2 processen van eenzelfde soort zijn.");
            props.put("org.quartz.threadPool.threadCount", threadCount);
        } else {
            props.put("org.quartz.threadPool.threadCount", "1");
        }
        props.put("org.quartz.scheduler.interruptJobsOnShutdownWithWait", "true");
        props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        props.put("org.quartz.scheduler.skipUpdateCheck", "true");
        log.debug("Start quartz scheduler met de opties: " + props);

        StdSchedulerFactory factory = new StdSchedulerFactory(props);
        scheduler = factory.getScheduler();
        scheduler.start();

        Stripersist.requestInit();
        EntityManager entityManager = Stripersist.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AutomatischProces> cq = cb.createQuery(AutomatischProces.class);
        Root<AutomatischProces> from = cq.from(AutomatischProces.class);
        cq.where(cb.isNotNull(from.get("cronExpressie")));
        List<AutomatischProces> procList = entityManager.createQuery(cq).getResultList();

        for (AutomatischProces p : procList) {
            addJobDetails(scheduler, p);
        }
        this.getServletConfig().getServletContext().setAttribute(QUARTZ_FACTORY_KEY, factory);
    }

    /**
     * voeg een job toe op basis van het proces.
     *
     * @param scheduler taakplanner van de brmo service
     * @param p het te bewerken automatisch proces
     * @throws SchedulerException als de bewerking mislukt
     */
    public static void addJobDetails(Scheduler scheduler, AutomatischProces p) throws SchedulerException {
        try {
            CronExpression cron = new CronExpression(p.getCronExpressie());

            JobDetail job = JobBuilder.newJob(AutomatischProcesJob.class)
                    .usingJobData("id", p.getId())
                    .withIdentity(JOBKEY_PREFIX + p.getId())
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .forJob(job)
                    .withIdentity(TRIGGERKEY_PREFIX + p.getId())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            scheduler.scheduleJob(job, trigger);

            log.info(String.format("%s met id: %d met cron expressie %s is toegevoegd (of bijgewerkt).",
                    p.getClass().getSimpleName(), p.getId(), p.getCronExpressie()));
        } catch (ParseException ex) {
            log.warn(String.format("Ongeldige cron expressie voor %s met id %d. Het proces is niet ingepland.",
                    p.getClass().getSimpleName(), p.getId()), ex);
        }
    }

}
