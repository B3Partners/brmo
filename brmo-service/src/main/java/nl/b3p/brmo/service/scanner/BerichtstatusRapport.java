/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BerichtstatusRapportProces;
import static nl.b3p.brmo.service.scanner.AbstractExecutableProces.OLD_LOG_LENGTH;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author mprins
 */
public class BerichtstatusRapport extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(MailRapportage.class);
    private BerichtstatusRapportProces config;
    @Transient
    private ProgressUpdateListener listener;

    public BerichtstatusRapport(BerichtstatusRapportProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                LOG.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        EntityManager em = Stripersist.getEntityManager();

        this.listener = listener;
        config.setStatus(PROCESSING);
        config.setLastrun(new Date());
        em.merge(config);
        em.flush();

        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog != null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog).append(AutomatischProces.LOG_NEWLINE);
            }
        }

        config.setStatus(PROCESSING);
        String msg = String.format("De Berichtstatus Rapportage met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        this.active = true;

        LOG.info("Ophalen bericht status informatie.");
        BrmoFramework brmo = null;
        long aantal = -1L;
        StringBuilder samenvatting = new StringBuilder("Berichtstatus overzicht");
        samenvatting.append(AutomatischProces.LOG_NEWLINE);
        try {
            brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);
            for (Bericht.STATUS status : Bericht.STATUS.values()) {
                aantal = brmo.getCountBerichten(null, null, null, status.name());
                msg = String.format("Aantal berichten met status %s: %s.", status.name(), aantal);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                samenvatting.append(msg).append(AutomatischProces.LOG_NEWLINE);
                LOG.info(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
            }
        } catch (BrmoException ex) {
            LOG.error(ex.getLocalizedMessage());
            listener.exception(ex);
            sb.append(ex.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }

        }
        msg = String.format("De Berichtstatus Rapportage met ID %d is afgerond op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        config.setSamenvatting(samenvatting.toString());
        config.setStatus(WAITING);
        config.setLastrun(new Date());
        config.setLogfile(sb.toString());
    }
}
