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
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.NULL;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ONBEKEND;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import static nl.b3p.brmo.persistence.staging.MailRapportageProces.PIDS;
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
     * ophalen van de automatisch proces entities.
     */
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
     * @return de default resolution
     *
     */
    @DefaultHandler
    public Resolution view() {
        if (log.isDebugEnabled()) {
            log.debug("=============== bekende BRK processen ===============");
            for (BRKScannerProces p : brkProcessen) {
                log.debug("  proces id: " + p.getId());
                log.debug("     status: " + p.getStatus());
                log.debug("   scan dir: " + p.getScanDirectory());
                log.debug("archief dir: " + p.getArchiefDirectory());
            }
            log.debug("=============== bekende BAG processen ===============");
            for (BAGScannerProces p : bagProcessen) {
                log.debug("  proces id: " + p.getId());
                log.debug("     status: " + p.getStatus());
                log.debug("   scan dir: " + p.getScanDirectory());
                log.debug("archief dir: " + p.getArchiefDirectory());
            }
            log.debug("=============== bekende MAIL processen===============");
            for (MailRapportageProces p : mailProcessen) {
                log.debug("  proces id: " + p.getId());
                log.debug("     status: " + p.getStatus());
                log.debug("   adressen: " + p.getMailAdressen());
                log.debug("  processen: " + p.getConfig().get(PIDS));
            }
        }

        return new ForwardResolution(JSP);
    }

    /**
     * Opslaan van de configuratie data.
     *
     * @return forward naar de default resolution
     */
    public Resolution save() {
        try {
            this.saveAll();
            getContext().getMessages().add(new SimpleMessage("Processen zijn succesvol opgeslagen."));
        } catch (Throwable t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het opslaan van de configuratie gegevens. {2}",
                            t.getMessage()));
            log.error("Er is een fout opgetreden tijdens opslaan van configuratie gegevens.", t);
        }
        return new ForwardResolution(JSP);
    }

    /**
     * Opslaan van de configuratie data en forceren van herladen van de data
     * uit. oa. zodat de pID bekend wordt.
     *
     * @return forward naar de default resolution
     */
    public Resolution addNew() {
        try {
            this.saveAll();
            this.load();
            getContext().getMessages().add(new SimpleMessage("Het nieuwe proces is succesvol opgeslagen."));
        } catch (Throwable t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het opslaan van de configuratie gegevens. {2}",
                            t.getMessage()));
            log.error("Er is een fout opgetreden tijdens opslaan van configuratie gegevens.", t);
        }
        return new ForwardResolution(JSP);
    }

    private void saveAll() {
        EntityManager em = Stripersist.getEntityManager();
        for (BRKScannerProces brk : this.brkProcessen) {
            em.merge(brk);
        }
        for (BAGScannerProces bag : this.bagProcessen) {
            em.merge(bag);
        }
        for (MailRapportageProces mail : this.mailProcessen) {
            em.merge(mail);
        }
        em.getTransaction().commit();
    }

    /**
     * Start een proces.
     *
     * @return forward naar de default resolution
     *
     */
    public Resolution startProces() {
        log.debug("Poging proces met pId: " + getContext().getRequest().getParameter("pId") + " te starten.");

        Long id = null;
        AutomatischProces config = null;
        final EntityManager em = Stripersist.getEntityManager();
        try {
            id = Long.valueOf(getContext().getRequest().getParameter("pId"));
            // ophalen proces config en starten proces
            config = em.find(AutomatischProces.class, id);
            ProcesExecutable p = AbstractExecutableProces.getProces(config);
            if (p.isRunning()) {
                getContext().getMessages().add(
                        new SimpleMessage("Proces met id {0} draait al.", id));
            } else {
                p.execute();
                getContext().getMessages().add(
                        new SimpleMessage("Het proces met ID {0} is afgerond.", id));
            }
        } catch (NumberFormatException t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het parsen van de proces id {2}.", id));
            log.error("Er is een fout opgetreden tijdens het parsen van de proces id.", t);
        } catch (BrmoException t) {
            getContext().getMessages().add(
                    new SimpleError("Er is een fout opgetreden tijdens het starten of uitvoeren van proces met id {2}. {3}",
                            id, t.getMessage()));
            log.error("Er is een fout opgetreden tijdens het starten of uitvoeren van het proces.", t);
            config.setStatus(ERROR);
        } finally {
            em.merge(config);
            em.getTransaction().commit();
        }
        return new ForwardResolution(JSP);
    }

    /**
     *
     * stopt het proces (voor zover mogelijk.)
     *
     ** @return forward naar de default resolution
     */
    public Resolution stopProces() {
        log.debug("Poging proces met pId: " + getContext().getRequest().getParameter("pId") + " te stoppen.");

        Long id = Long.valueOf(getContext().getRequest().getParameter("pId"));
        final EntityManager em = Stripersist.getEntityManager();
        AutomatischProces config = em.find(AutomatischProces.class, id);
        ProcesExecutable p = AbstractExecutableProces.getProces(config);
        if (p.isRunning()) {
            p.stop();
        }
        em.merge(config);
        em.getTransaction().commit();

        getContext().getMessages().add(new SimpleMessage("Proces {0} is gestopt.", id));
        return new ForwardResolution(JSP);
    }

    /**
     * Verwijderd het proces.
     *
     * @todo implementatie
     * @return forward naar de default resolution
     */
    public Resolution verwijderProces() {
        log.debug("Poging proces met pId: " + getContext().getRequest().getParameter("pId") + " te verwijderen.");

        Long id = Long.valueOf(getContext().getRequest().getParameter("pId"));
        final EntityManager em = Stripersist.getEntityManager();
        AutomatischProces config = em.find(AutomatischProces.class, id);
        em.remove(config);
        em.getTransaction().commit();
        // reload om de arraylists met entities te verversen
        this.load();

        getContext().getMessages().add(new SimpleMessage("Het proces met ID {0} is verwijderd.", id));;
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

    //</editor-fold>
}
