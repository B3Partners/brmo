/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BerichtDoorstuurProces;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import org.stripesstuff.plugin.waitpage.WaitPage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
public class BerichtDoorsturenUitvoerActionBean implements ActionBean, ProgressUpdateListener {

    private static final String JSP = "/WEB-INF/jsp/beheer/berichtdoorsturenuitvoeren.jsp";

    private ActionBeanContext context;

    @Validate(converter = EntityTypeConverter.class)
    private BerichtDoorstuurProces proces;

    private double progress;

    private long total;

    private long processed;

    private boolean complete;

    private String status;

    private String label;

    private String log = "";

    private Date start;

    private Date update;

    private String exceptionStacktrace;

    @Before
    public void before() {
        start = new Date();
    }

    @After
    public void completed() {
        complete = true;
    }

    @Override
    public void total(long total) {
        this.total = total;
    }

    @Override
    public void progress(long progress) {
        this.processed = progress;
        if (this.total != 0) {
            this.progress = (100.0 / this.total) * this.processed;
        }
        this.update = new Date();
    }

    @Override
    public void exception(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(new PrintWriter(sw));
        this.exceptionStacktrace = sw.toString();
    }

    @Override
    public void updateStatus(String status) {
        this.status = status;
    }

    @Override
    public void addLog(String log) {
        this.proces.addLogLine(log);
        //aangezien addLogLine een null-op is Stripersist.getEntityManager().merge(this.proces);
        this.log += log + "\n";
    }

    @DefaultHandler
    @WaitPage(path = JSP, delay = 1000, refresh = 1000)
    public Resolution execute() {

        if (proces == null) {
            getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
            completed();
            return new ForwardResolution(JSP);
        }
        // opnieuw laden van config omdat de waitpage de entity detached
        proces = (BerichtDoorstuurProces) Stripersist.getEntityManager().find(AutomatischProces.class, proces.getId());
        ProcesExecutable exeProces = AbstractExecutableProces.getProces(proces);

        try {
            exeProces.execute(this);
            completed();
        } finally {
            if (Stripersist.getEntityManager().getTransaction().isActive()) {
                Stripersist.getEntityManager().getTransaction().rollback();
            }
        }

        return new ForwardResolution(JSP);
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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProcessed() {
        return processed;
    }

    public void setProcessed(long processed) {
        this.processed = processed;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getUpdate() {
        return update;
    }

    public void setUpdate(Date update) {
        this.update = update;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }

    public BerichtDoorstuurProces getProces() {
        return proces;
    }

    public void setProces(BerichtDoorstuurProces proces) {
        this.proces = proces;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    // </editor-fold>
}
