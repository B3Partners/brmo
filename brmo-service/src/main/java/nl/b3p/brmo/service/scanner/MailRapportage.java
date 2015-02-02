/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;

import javax.mail.internet.InternetAddress;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import static nl.b3p.brmo.persistence.staging.MailRapportageProces.PIDS;
import static nl.b3p.brmo.service.scanner.AbstractExecutableProces.LOG_NEWLINE;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class MailRapportage extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(MailRapportage.class);
    private MailRapportageProces config;

    public MailRapportage(MailRapportageProces config) {
        this.config = config;
    }

    /**
     * @todo implementatie, zie TODO's
     * @throws BrmoException
     */
    @Override
    public void execute() throws BrmoException {
        this.active = true;
        StringBuilder sb = new StringBuilder();
        config.setStatus(PROCESSING);
        String logMsg = String.format("De mail rapportage met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        log.info(logMsg);
        sb.append(logMsg).append(LOG_NEWLINE);

        log.error("Het sturen van mail moet nog gebouwd worden.");
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", /* TODO - smtp server bepalen*/ "localhost");
//            Session session = Session.getInstance(props);
//            Message msg = new MimeMessage(session);
//            msg.setFrom(new InternetAddress(/* TODO - afzender email adres bepalen*/"noreply@b3p.nl"));

            // bepaal de ontvangers
            String[] adressen = this.config.getMailAdressenArray();
            InternetAddress[] aanAdr = new InternetAddress[adressen.length];
            for (int i = 0; i < adressen.length; i++) {
                aanAdr[i] = new InternetAddress(adressen[i]);
            }
//            msg.setRecipients(Message.RecipientType.TO, aanAdr);
//            msg.setSubject("BRMO rapportage");
//            msg.setSentDate(new Date());

            StringBuilder inhoud = new StringBuilder();
            String pids = config.getConfig().get(PIDS);
            //... TODO - wat gaat er allemaal in de inhoud van dit bericht??
            //           - alle samenvattigen van alle processen of een selectie van processen
            //           -
            EntityManager em = Stripersist.getEntityManager();
            List<AutomatischProces> processen = new ArrayList<AutomatischProces>();
            if (pids != null) {
                // ophalen van samenvattingen van geselecteerde processen
                Matcher m = (Pattern.compile("[0-9]+")).matcher(pids);
                while (m.find()) {
                    log.debug("Opzoeken van proces voor pid: " + m.group());
                    processen.add(em.find(AutomatischProces.class, Long.valueOf(m.group())));
                }
            } else {
                // ophalen samenvatting van alle processen want niet geselecteerd
                CriteriaQuery cq = em.getCriteriaBuilder().createQuery(AutomatischProces.class);
                Root<AutomatischProces> bagRoot = cq.from(AutomatischProces.class);
                processen = em.createQuery(cq).getResultList();
            }
            // bericht inhoud samenstellen
            for (AutomatischProces p : processen) {
                log.debug("bericht maken voor pid: " + p.getId() + " met samenvatting: " + p.getSamenvatting());
                inhoud.append("Rapport van taak: ").append(p.getId()).append(LOG_NEWLINE);
                inhoud.append(p.getSamenvatting()).append(LOG_NEWLINE);
            }

//            msg.setText(inhoud.toString());
//            Transport.send(msg);
            config.setStatus(WAITING);
            logMsg = String.format("Klaar met run op %tc", Calendar.getInstance());
            log.info(logMsg);
            sb.append(logMsg).append(LOG_NEWLINE);
            config.setSamenvatting(sb.toString());
            config.setLastrun(new Date());
        } catch (MessagingException ex) {
            log.error(ex);
            config.setStatus(ERROR);
            throw new BrmoException(ex);
        } finally {
            this.active = false;
        }
    }

}
