package nl.b3p.brmo.service.stripes;

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
import nl.b3p.brmo.persistence.staging.BGTLoaderProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

import java.util.Date;

public class BAG2MutatieProcesUitvoerenActionBean implements ActionBean, ProgressUpdateListener {
    private static final Log LOG = LogFactory.getLog(BAG2MutatieProcesUitvoerenActionBean.class);
    private static final String JSP = "/WEB-INF/jsp/beheer/bag2mutatieuitvoeren.jsp";
    private String exceptionStacktrace;
    private String status;
    private StringBuilder log = new StringBuilder();
    private ActionBeanContext context;
    @Validate(converter = EntityTypeConverter.class)
    private BGTLoaderProces proces;
    private Date start;
    private Date update;
    private boolean complete;

    @Before
    public void before() {
        this.start = new Date();
    }

    @After
    public void completed() {
        this.complete = true;
        this.update = new Date();
    }

    @DefaultHandler
    @WaitPage(path = JSP, delay = 1000, refresh = 1000)
    public Resolution execute() {
        if (proces == null) {
            getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
            completed();
            return new ForwardResolution(JSP);
        }

        throw new IllegalStateException("Not yet implemented");

/*
        // opnieuw laden van config omdat de waitpage de entity detached
        this.update = new Date();
        proces = (BAG2MutatieProces) Stripersist.getEntityManager().find(AutomatischProces.class, proces.getId());
        final ProcesExecutable _proces = AbstractExecutableProces.getProces(proces);
        try {
            _proces.execute(this);
        } finally {
            completed();
            Stripersist.getEntityManager().merge(proces);
            Stripersist.getEntityManager().getTransaction().commit();
        }
        return new ForwardResolution(JSP);
*/
    }

    @Override
    public void total(long total) {
    }

    @Override
    public void progress(long progress) {
        this.update = new Date();
    }

    @Override
    public void exception(Throwable t) {
        this.exceptionStacktrace = t.getLocalizedMessage();
    }

    @Override
    public void updateStatus(String status) {
        this.status = status;
    }

    @Override
    public void addLog(String line) {
        this.log.append(line).append("\n");
        LOG.info(log);
    }

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public BGTLoaderProces getProces() {
        return proces;
    }

    public void setProces(BGTLoaderProces proces) {
        this.proces = proces;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
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
    // </editor-fold>
}
