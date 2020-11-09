/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.LOG_NEWLINE;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.ClobElement;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Mailt rapporten.
 *
 * @author mprins
 */
public class MailRapportage extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(MailRapportage.class);
    private MailRapportageProces config;

    public MailRapportage(MailRapportageProces config) {
        this.config = config;
    }

    /**
     * De inhoud van dit bericht bestaat uit de samenvattingen en de status van
     * de geselecteerde processen, indien niets geselecteerd rapportage van
     * alles behalve het eigen proces.
     *
     * @throws BrmoException als er een fout optreed in het opzoeken van de
     * mailserver (indi) of het versturen van de mail
     */
    @Override
    public void execute() throws BrmoException {
        this.active = true;
        StringBuilder sb = new StringBuilder();
        config.setStatus(PROCESSING);
        String logMsg = String.format("De mail rapportage met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        log.info(logMsg);
        sb.append(logMsg).append(LOG_NEWLINE);

        try {
            Context init = new InitialContext();
            Context env = (Context) init.lookup("java:comp/env");
            Session session = (Session) env.lookup("mail/session");
            Message msg = new MimeMessage(session);
            msg.setFrom(/* 'mail.from' property uit jndi session object */);
            msg.setSentDate(new Date());
            // subject
            String sForStatus = (config.getForStatus() == null ? "alle" : config.getForStatus().toString());
            String sPIDS = (config.getConfig().get(MailRapportageProces.PIDS) == null ? "alle" : config.getConfig().get(MailRapportageProces.PIDS).getValue());
            String label = (ClobElement.nullSafeGet(config.getConfig().get("label")) == null ? "" : config.getConfig().get("label").getValue());
            String subject = String.format("BRMO rapport: %d (%s) voor status: %s (taken: %s)", config.getId(), label, sForStatus, sPIDS);
            msg.setSubject(subject);

            // ontvangers
            String[] adressen = this.config.getMailAdressenArray();
            InternetAddress[] aanAdr = new InternetAddress[adressen.length];
            for (int i = 0; i < adressen.length; i++) {
                aanAdr[i] = new InternetAddress(adressen[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, aanAdr);

            StringBuilder mailText = new StringBuilder();
            List<AutomatischProces> processen = this.getProcessen();
            // bericht mailText samenstellen
            for (AutomatischProces p : processen) {
                logMsg = String.format("Rapportage van taak %d met status: %s", p.getId(), p.getStatus());
                log.info(logMsg);
                sb.append(logMsg).append(LOG_NEWLINE);

                mailText.append("Rapport van taak: ").append(p.getId()).append(" ").append(label)
                        .append(", type: ").append(p.getClass().getSimpleName()).append(LOG_NEWLINE);
                mailText.append("Status van de taak: ").append(p.getStatus()).append(LOG_NEWLINE);
                mailText.append("Samenvatting: ").append(LOG_NEWLINE);
                mailText.append(p.getSamenvatting()).append(LOG_NEWLINE);
                mailText.append("---------------------------------").append(LOG_NEWLINE);
            }
            if (processen.isEmpty()) {
                mailText.append("Er waren geen automatische processen die voldeden aan de opgegeven voorwaarden: ").append(LOG_NEWLINE);
                mailText.append(" - Status: ").append(sForStatus).append(LOG_NEWLINE);
                mailText.append(" - Taken: ").append(sPIDS).append(LOG_NEWLINE);
            }
            log.debug(mailText.toString());
            msg.setText(mailText.toString());
            Transport.send(msg);

            logMsg = String.format("Klaar met taak %d op %tc", config.getId(), Calendar.getInstance());
            log.info(logMsg);
            sb.append(logMsg).append(LOG_NEWLINE);
            config.setStatus(WAITING);
            config.updateSamenvattingEnLogfile(sb.toString());
            config.setLastrun(new Date());
        } catch (MessagingException ex) {
            log.error(ex);
            config.setStatus(ERROR);
            throw new BrmoException(ex);
        } catch (NamingException ex) {
            log.error(ex);
            config.setStatus(ERROR);
            throw new BrmoException(ex);
        } finally {
            this.active = false;
        }
    }

    /**
     * Haalt de lijst met processen op uit de database.
     *
     * @return lijst met te rapporten processen
     * @todo Eventueel andere mail rapportage processen ook uitsluiten?
     */
    private List<AutomatischProces> getProcessen() {
        // unit test in nl.b3p.brmo.persistence.staging.MailRapportageProcesTest#testRapportageLijst()
        List<Predicate> predicates = new ArrayList<Predicate>();

        final EntityManager em = Stripersist.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AutomatischProces> cq = cb.createQuery(AutomatischProces.class);
        Root<AutomatischProces> from = cq.from(AutomatischProces.class);

        // where status=... filter
        if (this.config.getForStatus() != null) {
            Predicate where = cb.equal(from.get("status"), this.config.getForStatus());
            predicates.add(where);
        }
        // processen in... filter
        String pids = ClobElement.nullSafeGet(this.config.getConfig().get(MailRapportageProces.PIDS));
        if (pids != null) {
            List<Long> pidLijst = new ArrayList<Long>();
            Matcher match = (Pattern.compile("[0-9]+")).matcher(pids);
            while (match.find()) {
                pidLijst.add(Long.valueOf(match.group()));
            }
            Predicate in = from.get("id").in(pidLijst);
            predicates.add(in);
        }
        // TODO eventueel filter op dtype
        // niet het eigen proces rapporteren
        Predicate notIn = from.get("id").in(this.config.getId()).not();
        predicates.add(notIn);

        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // sorteer op status, daarna op datum zodat errors bovenaan staan
        cq.orderBy(cb.asc(from.get("status")));
        cq.orderBy(cb.asc(from.get("lastrun")));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        try {
            this.execute();
        } catch (BrmoException ex) {
            log.error(ex);
        }
    }
}
