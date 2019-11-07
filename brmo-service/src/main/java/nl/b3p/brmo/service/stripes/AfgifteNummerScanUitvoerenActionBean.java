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
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
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
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class AfgifteNummerScanUitvoerenActionBean implements ActionBean, ProgressUpdateListener {

    private static final String JSP = "/WEB-INF/jsp/beheer/afgiftenummerscanneruitvoeren.jsp";

    @Validate(converter = EntityTypeConverter.class)
    private AfgifteNummerScannerProces proces;

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
        AfgifteNummerScanner _proces = (AfgifteNummerScanner) AbstractExecutableProces.getProces(proces);
        try {
            _proces.execute(this);
            getContext().getMessages().add(new SimpleMessage("Afgiftenummer scan is afgerond."));
        } catch (Exception ex) {
            proces.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het bepalen van ontbrekende afgiftenummers. {2}",
                            ex.getMessage()));
        } finally {
            proces.updateSamenvattingEnLogfile(this.log.toString());
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
    // </editor-fold>
}
