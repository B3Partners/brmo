/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.AfgifteNummerScannerProces;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.AfgifteNummerScanner;
import nl.b3p.brmo.service.scanner.ProgressUpdateListener;
import org.stripesstuff.plugin.waitpage.WaitPage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 * actionbean voor uitvoeren van afgiftenummer scan.
 *
 * @since 2.0.0
 * @author mprins
 */
public class AfgifteNummerScanUitvoerenActionBean implements ActionBean, ProgressUpdateListener {

  private static final String JSP = "/WEB-INF/jsp/beheer/afgiftenummerscanneruitvoeren.jsp";

  @Validate(converter = EntityTypeConverter.class)
  private AfgifteNummerScannerProces proces;

  private ActionBeanContext context;

  private String exceptionStacktrace;

  private String status;

  private long total;

  private boolean complete;

  private Date start;

  private Date update;

  private StringBuilder log = new StringBuilder();

  @DefaultHandler
  @WaitPage(path = JSP, delay = 1000, refresh = 1000)
  public Resolution execute() {
    String samenvatting = null;

    if (proces == null) {
      getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
      completed();
      return new ForwardResolution(JSP);
    }

    proces = Stripersist.getEntityManager().find(AfgifteNummerScannerProces.class, proces.getId());
    AfgifteNummerScanner _proces =
        (AfgifteNummerScanner) AbstractExecutableProces.getProces(proces);

    try {
      _proces.execute(this);
      this.proces.setOntbrekendeNummersGevonden(_proces.getOntbrekendeNummersGevonden());
      getContext().getMessages().add(new SimpleMessage("Afgiftenummer scan is afgerond."));
      samenvatting =
          "Er zijn "
              + (_proces.getOntbrekendeNummersGevonden() ? "" : "geen")
              + " ontbrekende afgiftenummers geconstateerd.";
      getContext().getMessages().add(new SimpleMessage(samenvatting));
    } catch (Exception ex) {
      this.proces.setStatus(AutomatischProces.ProcessingStatus.ERROR);
      Stripersist.getEntityManager().merge(this.proces);
      exception(ex);
      samenvatting =
          "Er is een fout opgetreden tijdens het bepalen van ontbrekende afgiftenummers.";
      getContext()
          .getMessages()
          .add(
              new SimpleError(
                  "Er is een fout opgetreden tijdens het bepalen van ontbrekende afgiftenummers. {2}",
                  ex.getMessage()));
    } finally {
      this.completed();
      this.proces.updateSamenvattingEnLogfile(this.log.toString());
      // de hele log is te groot voor de samenvatting, maar #updateSamenvattingEnLogfile is
      // wel handig om te gebruiken
      this.proces.setSamenvatting(samenvatting);

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
    // this.processed = progress;
    this.total = progress;
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

  // <editor-fold defaultstate="collapsed" desc="getters en setters">
  @Override
  public void setContext(ActionBeanContext context) {
    this.context = context;
  }

  @Override
  public ActionBeanContext getContext() {
    return this.context;
  }

  public AfgifteNummerScannerProces getProces() {
    return proces;
  }

  public void setProces(AfgifteNummerScannerProces proces) {
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
  // </editor-fold>
}
