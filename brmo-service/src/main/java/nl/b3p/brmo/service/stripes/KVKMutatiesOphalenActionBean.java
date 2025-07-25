package nl.b3p.brmo.service.stripes;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.KVKMutatieserviceProcesRunner;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

public class KVKMutatiesOphalenActionBean implements ActionBean, ProgressUpdateListener {
  private static final String JSP = "/WEB-INF/jsp/beheer/kvkmutatieserviceprocesuitvoeren.jsp";
  private static final Log LOG = LogFactory.getLog(KVKMutatiesOphalenActionBean.class);

  @Validate(converter = EntityTypeConverter.class)
  private KVKMutatieserviceProces proces;

  private ActionBeanContext context;
  private String exceptionStacktrace;
  private String status;
  private long total;
  private long processed;
  private double progress;
  private boolean complete;
  private Date start;
  private Date update;
  private StringBuilder log = new StringBuilder();

  @DefaultHandler
  @WaitPage(path = JSP, delay = 1000, refresh = 1000)
  public Resolution execute() {
    String samenvatting;

    if (proces == null) {
      samenvatting = "Geen geldig KVK Mutatieservice proces opgegeven.";
      getContext().getMessages().add(new SimpleMessage(samenvatting));
      LOG.warn(samenvatting);
      completed();
      return new ForwardResolution(JSP);
    }

    // opnieuw laden van config omdat de waitpage de entity detached
    proces =
        (KVKMutatieserviceProces)
            Stripersist.getEntityManager().find(AutomatischProces.class, proces.getId());
    ProcesExecutable runner = AbstractExecutableProces.getProces(proces);
    try {
      runner.execute(this);
    } catch (Exception ex) {
      proces.setStatus(ERROR);
      exception(ex);
      samenvatting = "Er is een fout opgetreden tijdens het ophalen van KVK mutaties.";
      this.addLog(samenvatting);
      getContext()
          .getMessages()
          .add(
              new SimpleError(
                  "Er is een fout opgetreden tijdens het ophalen van KVK mutaties. {2}",
                  ex.getMessage()));
    } finally {
      completed();
      proces.updateSamenvattingEnLogfile(this.log.toString());
      Stripersist.getEntityManager().merge(proces);
      Stripersist.getEntityManager().flush();
      if (Stripersist.getEntityManager().getTransaction().isActive()) {
        Stripersist.getEntityManager().getTransaction().commit();
      }
    }
    return new ForwardResolution(JSP);
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
    KVKMutatieserviceProcesRunner.getLog().info(line);
  }

  @Before
  public void before() {
    start = new Date();
  }

  @After
  public void completed() {
    complete = true;
    proces.setLastrun(this.update == null ? new Date() : this.update);
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

  public KVKMutatieserviceProces getProces() {
    return proces;
  }

  public void setProces(KVKMutatieserviceProces proces) {
    this.proces = proces;
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

  public long getTotal() {
    return this.total;
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

  public double getProgress() {
    return progress;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  // </editor-fold>
}
