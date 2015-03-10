/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.service.jobs;

import javax.persistence.EntityManager;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Job;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class AutomatischProcesJob implements Job {

    private static final Log log = LogFactory.getLog(AutomatischProcesJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long id = context.getJobDetail().getJobDataMap().getLongValue("id");
        log.debug("Poging gepland automatisch proces met id: " + id + " te starten.");

        try {
            Stripersist.requestInit();
            EntityManager em = Stripersist.getEntityManager();
            AutomatischProces p = em.find(AutomatischProces.class, id);
            p.addLogLine("Geplande taak gestart.");
            AbstractExecutableProces.getProces(p).execute();
            p.addLogLine("Geplande taak afgerond.");
            em.merge(p);
            em.getTransaction().commit();
            log.debug("Gepland automatisch proces met " + id + " is afgerond.");
        } catch (Exception ex) {
            log.error("Fout tijdens starten of uitvoeren van automatische proces met id: " + id, ex);
        }
    }
}
