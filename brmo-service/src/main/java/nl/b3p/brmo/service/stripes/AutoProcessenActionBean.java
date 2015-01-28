/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.SimpleError;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ONBEKEND;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import nl.b3p.brmo.service.scanner.AbstractExecutableProces;
import nl.b3p.brmo.service.scanner.ProcesExecutable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins
 */
public class AutoProcessenActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(AutoProcessenActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/beheer/processen.jsp";

    private ActionBeanContext context;

    private List<BRKScannerProces> brkProcessen = new ArrayList<BRKScannerProces>();

    private List<BAGScannerProces> bagProcessen = new ArrayList<BAGScannerProces>();

    private List<MailRapportageProces> mailProcessen = new ArrayList<MailRapportageProces>();

    /**
     * identifier van het proces waar we mee werken.
     *
     * @see nl.b3p.brmo.persistence.staging.AutomatischProces#getId()
     */
    private long procesId;

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void load() {
        EntityManager em = Stripersist.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery brkCrit = cb.createQuery(BRKScannerProces.class);
        Root<BRKScannerProces> brkRoot = brkCrit.from(BRKScannerProces.class);
        TypedQuery<BRKScannerProces> qBrk = em.createQuery(brkCrit);
        brkProcessen = qBrk.getResultList();

        CriteriaQuery bagCrit = cb.createQuery(BAGScannerProces.class);
        Root<BAGScannerProces> bagRoot = bagCrit.from(BAGScannerProces.class);
        TypedQuery<BAGScannerProces> qBag = em.createQuery(bagCrit);
        bagProcessen = qBag.getResultList();

        CriteriaQuery mailCrit = cb.createQuery(MailRapportageProces.class);
        Root<MailRapportageProces> mRoot = mailCrit.from(MailRapportageProces.class);
        TypedQuery<MailRapportageProces> qMail = em.createQuery(mailCrit);
        mailProcessen = qMail.getResultList();
    }

    /**
     * uitlezen van de configuratie data.
     *
     * @return
     *
     */
    @DefaultHandler
    public Resolution view() {
        try {

            if (log.isDebugEnabled()) {
                // TODO als er geen is maken we een lege aan
                if (brkProcessen.isEmpty()) {
                    EntityManager em = Stripersist.getEntityManager();
                    BRKScannerProces b = new BRKScannerProces();
                    b.setScanDirectory("scan directory");
                    b.setArchiefDirectory("Archief directory");
                    b.setStatus(ONBEKEND);
                    em.persist(b);
                    brkProcessen.add(b);
                    em.getTransaction().commit();
                }

                for (BRKScannerProces p : brkProcessen) {
                    log.debug("  proces id: " + p.getId());
                    log.debug("     status: " + p.getStatus());
                    log.debug("   scan dir: " + p.getScanDirectory());
                    log.debug("archief dir: " + p.getArchiefDirectory());
                }
            }

            //... etcetera ...
        } catch (Exception ex) {
            log.error("TODO foutmelding", ex);
        }
        // andere config items lezen??
        return new ForwardResolution(JSP);
    }

    /**
     * Opslaan van de configuratie data
     *
     * @return
     *
     * @todo implement
     */
    public Resolution save() {
        try {
            EntityManager em = Stripersist.getEntityManager();
            for (BRKScannerProces brk : this.brkProcessen) {
                em.persist(brk);
            }
            for (BAGScannerProces bag : this.bagProcessen) {
                em.persist(bag);
            }
            for (MailRapportageProces mail : this.mailProcessen) {
                em.persist(mail);
            }
            em.getTransaction().commit();
            getContext().getMessages().add(new SimpleMessage("Proces is succesvol opgeslagen."));
        } catch (Throwable t) {
            getContext().getMessages().add(new SimpleError("Er is een fout opgetreden tijdens het opslaan van de configuratie gegevens. {2}", t.getMessage()));
            log.error("Er is een fout opgetreden tijdens opslaan van configuratie gegevens.", t);
        }
        // return to the form
        return new ForwardResolution(JSP);
    }

    /**
     *
     * @return
     *
     * @todo implement
     */
    public Resolution startProces() {
        log.debug("Start proces met pId: " + getContext().getRequest().getParameter("pId"));

        Long id = null;
        try {
            id = Long.valueOf(getContext().getRequest().getParameter("pId"));
            // ophalen proces config en starten proces
            AutomatischProces config = Stripersist.getEntityManager().find(AutomatischProces.class, id);
            ProcesExecutable p = AbstractExecutableProces.getProces(config);
            p.execute();
            Stripersist.getEntityManager().persist(config);
        } catch (NumberFormatException t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het parsen van de proces id {2}. {3}",
                            id, t.getMessage()));
            log.error("Er is een fout opgetreden tijdens het parsen van de proces id.", t);
        } catch (BrmoException t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het starten of uitvoeren van proces {2}. {3}",
                            id, t.getMessage()));
            log.error("Er is een fout opgetreden tijdens het starten of uitvoeren van het proces.", t);
        } finally {

        }
        // terug naar de pagina of een overzicht van lopende processen?
        return new ForwardResolution(JSP);
    }

    /**
     *
     *
     * @return
     *
     * @todo implement
     */
    public Resolution stopProces() {
        log.debug("context: " + getContext().toString());
        log.debug("pId" + getContext().getRequest().getParameter("pId"));
        log.debug("proces type voor stop is : " + getContext().getRequest().getParameter(" pType"));

        getContext().getMessages().add(new SimpleError("TODO {2} is nog niet geimplementeerd...", getContext()));
        return new ForwardResolution(JSP);
    }

    /**
     *
     * @return
     */
    public Resolution verwijderProces() {
        log.debug("context: " + getContext().toString());
        log.debug("id" + getContext().getRequest().getParameter("id"));
        log.debug("proces id voor start is : " + procesId);

        getContext().getMessages().add(new SimpleError("TODO {2} is nog niet geimplementeerd...", getContext()));
        return new ForwardResolution(JSP);
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * {@inheritDoc }
     */
    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    public List<BRKScannerProces> getBrkProcessen() {
        return brkProcessen;
    }

    public void setBrkProcessen(List<BRKScannerProces> brkProcessen) {
        this.brkProcessen = brkProcessen;
    }

    public List<MailRapportageProces> getMailProcessen() {
        return mailProcessen;
    }

    public void setMailProcessen(List<MailRapportageProces> mailProcessen) {
        this.mailProcessen = mailProcessen;
    }

    public List<BAGScannerProces> getBagProcessen() {
        return bagProcessen;
    }

    public void setBagProcessen(List<BAGScannerProces> bagProcessen) {
        this.bagProcessen = bagProcessen;
    }

    public long getProcesId() {
        return procesId;
    }

    public void setProcesId(long procesId) {
        this.procesId = procesId;
    }
    //</editor-fold>
}
