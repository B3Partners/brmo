/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.MailRapportage;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author mprins
 */
@StrictBinding
public class MailProcesUitvoerActionBean implements ActionBean {

    private static final String JSP = "/WEB-INF/jsp/beheer/mailprocesuitvoeren.jsp";

    @Validate(converter = EntityTypeConverter.class)
    private MailRapportageProces proces;

    private ActionBeanContext context;

    @DefaultHandler
    public Resolution execute() {
        if (proces == null) {
            getContext().getMessages().add(new SimpleMessage("Proces ongeldig!"));
            return new ForwardResolution(JSP);
        }
        MailRapportage _proces = (MailRapportage) AbstractExecutableProces.getProces(proces);
        try {
            _proces.execute();
            getContext().getMessages().add(new SimpleMessage("De mail is verstuurd."));
        } catch (BrmoException ex) {
            proces.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het verturen van de mail. {2}",
                            ex.getMessage()));
        } finally {
            Stripersist.getEntityManager().merge(proces);
            Stripersist.getEntityManager().getTransaction().commit();
        }
        return new ForwardResolution(JSP);
    }

    public MailRapportageProces getProces() {
        return proces;
    }

    public void setProces(MailRapportageProces proces) {
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

}
