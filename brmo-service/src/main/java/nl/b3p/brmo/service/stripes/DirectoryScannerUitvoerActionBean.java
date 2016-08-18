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
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import org.stripesstuff.plugin.waitpage.WaitPage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author mprins
 */
@StrictBinding
public class DirectoryScannerUitvoerActionBean implements ActionBean, ProgressUpdateListener {

    private static final String JSP = "/WEB-INF/jsp/beheer/dirscanuitvoeren.jsp";

    @Validate(converter = EntityTypeConverter.class)
    private AutomatischProces proces;

    private ActionBeanContext context;

    private double progress;

    private long total;

    private long processed;

    private boolean complete;

    private String status;

    private String label;

    private StringBuilder log = new StringBuilder();
    private int logLineCounter = 0;

    private Date start;

    private Date update;

    private String exceptionStacktrace;

    @DefaultHandler
    @WaitPage(path = JSP, delay = 1000, refresh = 1000)
    public Resolution execute() {
        if (proces == null) {
            getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
            completed();
            return new ForwardResolution(JSP);
        }
        // opnieuw laden van config omdat de waitpage de entity detached
        proces = Stripersist.getEntityManager().find(AutomatischProces.class, proces.getId());
        ProcesExecutable exeProces = AbstractExecutableProces.getProces(proces);
        try {
            exeProces.execute(this);
            this.proces.setStatus(AutomatischProces.ProcessingStatus.WAITING);
        } catch (Exception e) {
            this.proces.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            Stripersist.getEntityManager().merge(this.proces);
            exception(e);
        } finally {
            completed();
            if (Stripersist.getEntityManager().getTransaction().isActive()) {
                Stripersist.getEntityManager().getTransaction().commit();
            }
        }

        return new ForwardResolution(JSP);
    }

    @Before
    public void before() {
        start = new Date();
    }

    @After
    public void completed() {
        complete = true;
        proces.setLastrun(this.update);
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
        // voorkom dubbele log uitvoer
        // this.proces.addLogLine(log);
        // Stripersist.getEntityManager().merge(this.proces);
        if (this.logLineCounter > 1000) {
            // trim buffer
            int i900regels = 100;
            this.log.delete(0, i900regels);
            this.log.trimToSize();
        }
        this.log.append(log).append("\n");
        this.logLineCounter++;
    }

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public AutomatischProces getProces() {
        return proces;
    }

    public void setProces(AutomatischProces proces) {
        this.proces = proces;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
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

    public String getLog() {
        return log.toString();
    }

    public void setLog(String log) {
        this.log = new StringBuilder(log);
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

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }

}
