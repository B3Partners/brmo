/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Mark Prins <mark@b3partners.nl>
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
            log.error(ex);
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
        props.put("org.quartz.threadPool.threadCount", "1");
        props.put("org.quartz.scheduler.interruptJobsOnShutdownWithWait", "true");
        props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        props.put("org.quartz.scheduler.skipUpdateCheck", "true");

        StdSchedulerFactory factory = new StdSchedulerFactory(props);

        scheduler = factory.getScheduler();
        scheduler.startDelayed(60);

        Stripersist.requestInit();
        EntityManager entityManager = Stripersist.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AutomatischProces> cq = cb.createQuery(AutomatischProces.class);
        Root<AutomatischProces> from = cq.from(AutomatischProces.class);
        cq.where(cb.isNotNull(from.get("cron_expressie")));
        List<AutomatischProces> procList = entityManager.
                createQuery(cq).getResultList();

        for (AutomatischProces p : procList) {
            String _msg = String.format("toevoegen van proces: %d met cron expressie %s",
                    p.getId(), p.getCron_expressie());
            log.debug(_msg);
            addJobDetails(scheduler, p);
        }
        this.getServletConfig().getServletContext().setAttribute(QUARTZ_FACTORY_KEY, factory);
    }

    /**
     * voeg een job toe op basis van het proces.
     *
     * @param scheduler
     * @param p
     * @throws SchedulerException
     */
    public static void addJobDetails(Scheduler scheduler, AutomatischProces p) throws SchedulerException {

        try {
            CronExpression cron = new CronExpression(p.getCron_expressie());

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

        } catch (ParseException ex) {
            log.error("Ongeldige cron expressie voor proces met id: " + p.getId(), ex);
        }
    }

}
