/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationMethod;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.persistence.staging.BGTLightOphaalProces;
import nl.b3p.brmo.persistence.staging.BGTLightScannerProces;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.BerichtDoorstuurProces;
import nl.b3p.brmo.persistence.staging.BerichtTransformatieProces;
import nl.b3p.brmo.persistence.staging.BerichtstatusRapportProces;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.GDS2OphaalProces;
import nl.b3p.brmo.persistence.staging.LaadprocesTransformatieProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import nl.b3p.brmo.persistence.staging.MaterializedViewRefresh;
import nl.b3p.brmo.persistence.staging.TopNLScannerProces;
import nl.b3p.brmo.persistence.staging.WebMirrorBAGScannerProces;
import nl.b3p.brmo.service.jobs.GeplandeTakenInit;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.QUARTZ_FACTORY_KEY;
import static nl.b3p.brmo.service.jobs.GeplandeTakenInit.SCHEDULER_NAME;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author mprins
 */
@StrictBinding
public class OphaalConfigActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(OphaalConfigActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/beheer/processenophalen.jsp";

    private ActionBeanContext context;

    private List<AutomatischProces> processen = new ArrayList();

    @Validate(converter = EntityTypeConverter.class)
    @ValidateNestedProperties({
        @Validate(field = "cronExpressie", on = "save")
    })
    private AutomatischProces proces;

    @Validate
    private ProcesExecutable.ProcessingImple type;

    @Validate
    private Map<String, ClobElement> config = new HashMap<String, ClobElement>();

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void load() {
        processen = Stripersist.getEntityManager().createQuery("from AutomatischProces p order by type(p),p.id").getResultList();
    }

    @DefaultHandler
    public Resolution view() {
        if (proces != null) {
            config = proces.getConfig();
        }

        return new ForwardResolution(JSP);
    }

    @DontValidate
    public Resolution cancel() {
        return new RedirectResolution(OphaalConfigActionBean.class);
    }

    @DontValidate
    public Resolution delete() {
        if (proces != null) {

            Stripersist.getEntityManager().createQuery("update LaadProces set automatischProces = null where automatischProces = :this")
                    .setParameter("this", proces)
                    .executeUpdate();

            Stripersist.getEntityManager().remove(proces);
            load();
            Stripersist.getEntityManager().getTransaction().commit();
            deleteScheduledJob(proces);
            proces = null;
            getContext().getMessages().add(new SimpleMessage("Proces is verwijderd"));
        }
        return new ForwardResolution(JSP);
    }

    @DontValidate
    public Resolution add() {
        return new ForwardResolution(JSP).addParameter("type", type);
    }

    public Resolution save() {
        if (proces == null) {
            proces = getProces(type);
        }

        proces.getConfig().clear();
        proces.getConfig().putAll(config);

        if(proces instanceof BerichtDoorstuurProces) {
            String id = ClobElement.nullSafeGet(config.get("gds2_ophaalproces_id"));
            GDS2OphaalProces p = Stripersist.getEntityManager().find(GDS2OphaalProces.class, Long.parseLong(id));
            if(p != null) {
                String label = ClobElement.nullSafeGet(p.getConfig().get("label"));
                proces.getConfig().put("label", new ClobElement("Doorsturen " + label + " afgiftes"));
            }
        }

        Stripersist.getEntityManager().persist(proces);
        load();
        Stripersist.getEntityManager().getTransaction().commit();
        getContext().getMessages().add(new SimpleMessage("Proces is opgeslagen"));

        try {
            this.updateJobSchedule(proces);
        } catch (SchedulerException ex) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens inplannen van de taak. {2}",
                            ex.getMessage()));
        }

        return new ForwardResolution(JSP).addParameter("proces", proces.getId());
    }

    /**
     * ProcesExecutable factory.
     *
     * @param type the type of {@code AutomatischProces} to create.
     * @return an instance of the specified type
     *
     */
    private AutomatischProces getProces(ProcesExecutable.ProcessingImple type) {
        switch (type) {
            case BAGScannerProces:
                return new BAGScannerProces();
            case BRKScannerProces:
                return new BRKScannerProces();
            case MailRapportageProces:
                return new MailRapportageProces();
            case GDS2OphaalProces:
                return new GDS2OphaalProces();
            case BerichtTransformatieProces:
                return new BerichtTransformatieProces();
            case BerichtDoorstuurProces:
                return new BerichtDoorstuurProces();
            case WebMirrorBAGScannerProces:
                return new WebMirrorBAGScannerProces();
            case BGTLightScannerProces:
                return new BGTLightScannerProces();
            case BGTLightOphaalProces:
                return new BGTLightOphaalProces();
            case LaadprocesTransformatieProces:
                return new LaadprocesTransformatieProces();
            case MaterializedViewRefresh:
                return new MaterializedViewRefresh();
            case BerichtstatusRapportProces:
                return new BerichtstatusRapportProces();
            case TopNLScannerProces:
                return new TopNLScannerProces();
            default:
                throw new IllegalArgumentException(type.name() + " is geen ondersteund proces type...");
        }
    }

    private ProcesExecutable.ProcessingImple getType(AutomatischProces p) {
        return ProcesExecutable.ProcessingImple.valueOf(p.getClass().getSimpleName());
    }

    /**
     * Vervang de proces job door een aangepaste job of plaats een nieuwe job mocht
     * deze nog niet bestaan. Als de cron expressie {@code null} is wordt de job
     * verwijderd uit de scheduler.
     *
     * @param p bij te werken proces
     * @throws SchedulerException
     */
    private void updateJobSchedule(AutomatischProces p) throws SchedulerException {
        log.debug("Update scheduled job:" + p.getId());

        StdSchedulerFactory factory = (StdSchedulerFactory) getContext().getServletContext()
                .getAttribute(QUARTZ_FACTORY_KEY);
        Scheduler scheduler = factory.getScheduler(SCHEDULER_NAME);
        JobKey key = new JobKey(GeplandeTakenInit.JOBKEY_PREFIX + p.getId());
        log.debug("Jobkey voor id " + p.getId() + " gevonden? " + scheduler.checkExists(key));

        scheduler.deleteJob(key);
        if (p.getCronExpressie() != null) {
            GeplandeTakenInit.addJobDetails(scheduler, p);
        }
    }

    /**
     * verwijder een proces job uit de scheduler door update met een {@code null} cron
     * schedule.
     *
     * @param p te verwijderen proces
     */
    private void deleteScheduledJob(AutomatischProces p) {
        try {
            p.setCronExpressie(null);
            updateJobSchedule(p);
        } catch (SchedulerException se) {
            log.warn("Ingeplande taak uit de planner halen is mislukt", se);
        }
    }

    /**
     * validatie van de cron expressie van een proces voorafgaand aan save.
     */
    @ValidationMethod(on = "save")
    public void validateCronExpressie() {
        if (proces != null) {
            String expr = proces.getCronExpressie();
            try {
                if (expr != null) {
                    CronExpression cron = new CronExpression(expr);
                }
            } catch (ParseException ex) {
                Stripersist.getEntityManager().refresh(proces);
                load();
                getContext().getValidationErrors().add("cronExpressie",
                        new SimpleError("{0} {2} is ongeldig, (melding: {4}, mogelijk nabij positie {3})",
                                expr, ex.getErrorOffset(), ex.getLocalizedMessage()));
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<AutomatischProces> getProcessen() {
        return processen;
    }

    public void setProcessen(List<AutomatischProces> processen) {
        this.processen = processen;
    }

    public AutomatischProces getProces() {
        return proces;
    }

    public void setProces(AutomatischProces proces) {
        this.proces = proces;
        this.type = getType(proces);
    }

    public Map<String, ClobElement> getConfig() {
        return config;
    }

    public void setConfig(Map<String, ClobElement> config) {
        this.config = config;
    }

    public ProcesExecutable.ProcessingImple getType() {
        return type;
    }

    public void setType(ProcesExecutable.ProcessingImple type) {
        this.type = type;
        this.proces = getProces(type);
    }
    // </editor-fold>
}
