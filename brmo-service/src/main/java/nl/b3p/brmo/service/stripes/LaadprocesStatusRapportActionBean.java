/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.LaadprocesStatusRapportProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.LaadprocesStatusRapport;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author mprins
 */
public class LaadprocesStatusRapportActionBean implements ActionBean, ProgressUpdateListener {

    private static final String JSP = "/WEB-INF/jsp/beheer/laadprocesstatusrapportuitvoeren.jsp";

    @Validate(converter = EntityTypeConverter.class)
    private LaadprocesStatusRapportProces proces;

    private ActionBeanContext context;

    private String exceptionStacktrace;
    private String status;
    private StringBuilder log = new StringBuilder();

    @DefaultHandler
    public Resolution execute() {
        if (proces == null) {
            getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
            return new ForwardResolution(JSP);
        }
        LaadprocesStatusRapport _proces = (LaadprocesStatusRapport) AbstractExecutableProces.getProces(proces);
        try {
            _proces.execute(this);
        } finally {
            Stripersist.getEntityManager().merge(proces);
            Stripersist.getEntityManager().getTransaction().commit();
        }
        return new ForwardResolution(JSP);
    }

    @Override
    public void total(long total) {
    }

    @Override
    public void progress(long progress) {
    }

    @Override
    public void exception(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        this.exceptionStacktrace = sw.toString();
    }

    @Override
    public void updateStatus(String status) {
        this.status = status;
    }

    @Override
    public void addLog(String line) {
        this.log.append(line).append("\n");
    }

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    public LaadprocesStatusRapportProces getProces() {
        return proces;
    }

    public void setProces(LaadprocesStatusRapportProces proces) {
        this.proces = proces;
    }

    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String getLog() {
        return log.toString();
    }

    public void setLog(String log) {
        this.log = new StringBuilder(log);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }
    // </editor-fold>

}
