/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BerichtTransformatieProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Transformatie van berichten in een automatisch proces gebruikmakend van
 * BrmoFramework.
 *
 * @see nl.b3p.brmo.loader.BrmoFramework
 *
 * @author mprins
 */
public class BerichtTransformatieUitvoeren extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BerichtTransformatieUitvoeren.class);

    private final BerichtTransformatieProces config;

    private ProgressUpdateListener l;

    private boolean transformErrorOccured = false;

    public BerichtTransformatieUitvoeren(BerichtTransformatieProces config) {
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
                // afvangen van de fout conditie zodat de status van het proces goed kan worden gezet
                transformErrorOccured = true;
                log.error(t);
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
        this.l = listener;
        l.updateStatus("Initialiseren...");
        l.addLog(String.format("Initialiseren... %tc", new Date()));
        this.config.setStatus(PROCESSING);
        this.config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        final StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog != null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog);
            }
        }

        String msg = String.format("Het bericht transformatie proces met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        log.info(msg);
        l.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        BrmoFramework brmo = null;
        try {
            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            DataSource dataSourceRsgb = ConfigUtil.getDataSourceRsgb();
            brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);
            brmo.setEnablePipeline(true);
            brmo.setOrderBerichten(true);
            brmo.setTransformPipelineCapacity(100);

            Thread t = brmo.toRsgb(new nl.b3p.brmo.loader.ProgressUpdateListener() {
                @Override
                public void total(long total) {
                    sb.append("Totaal te transformeren: ")
                            .append(total).append(AutomatischProces.LOG_NEWLINE);
                    l.total(total);
                }

                @Override
                public void progress(long progress) {
                    l.progress(progress);
                }

                @Override
                public void exception(Throwable t) {
                    sb.append("Fout tijdens transformeren: ")
                            .append(t.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
                    l.exception(t);
                }
            });
            t.join();

            if (transformErrorOccured) {
                this.config.setStatus(ERROR);
                msg = "Handmatige transformatie vanuit de berichten pagina is noodzakelijk.";
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                log.warn(msg);
                msg = String.format("Bericht transformatie proces met ID %d is niet succesvol afgerond op %tc.", config.getId(), Calendar.getInstance());
                log.error(msg);
            } else {
                this.config.setStatus(WAITING);
                msg = String.format("Bericht transformatie proces met ID %d is succesvol afgerond op %tc.", config.getId(), Calendar.getInstance());
                log.info(msg);
            }
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            l.addLog(msg);
            this.config.setSamenvatting(msg);
            this.config.setLogfile(sb.toString());
        } catch (BrmoException | InterruptedException t) {
            log.error("Fout bij transformeren berichten naar RSGB", t);
            String m = "Fout bij transformeren berichten naar RSGB: " + ExceptionUtils.getMessage(t);
            if (t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            this.config.setLogfile(sb.toString());
            this.config.updateSamenvattingEnLogfile(m);
            this.config.setStatus(ERROR);
        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().flush();
        }
    }
}
