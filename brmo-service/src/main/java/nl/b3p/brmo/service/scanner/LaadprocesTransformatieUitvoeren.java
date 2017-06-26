/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Transient;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.LaadprocesTransformatieProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Transformeert laadprocessn, zoals bijvoorbeeld BGTLight GML.
 *
 * @author mprins
 */
public class LaadprocesTransformatieUitvoeren extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(LaadprocesTransformatieUitvoeren.class);

    private final LaadprocesTransformatieProces config;

    @Transient
    private ProgressUpdateListener listener;

    private boolean transformErrorOccured = false;

    public LaadprocesTransformatieUitvoeren(LaadprocesTransformatieProces config) {
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
    public void execute(final ProgressUpdateListener listener) {
        this.listener = listener;
        listener.updateStatus("Initialiseren...");
        listener.addLog(String.format("Initialiseren... %tc", new Date()));
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

        String msg = String.format("Het laadproces transformatie proces met ID %d is gestart op %tc.",
                config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        BrmoFramework brmo = null;
        try {
            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            DataSource dataSourceRsgbBgt = ConfigUtil.getDataSourceRsgbBgt();
            DataSource dataSourceTopNL = ConfigUtil.getDataSourceTopNL();
            brmo = new BrmoFramework(dataSourceStaging, null, dataSourceRsgbBgt, dataSourceTopNL);
            long[] lpIds = null;
            
            if(LaadprocesTransformatieProces.LaadprocesSoorten.BR_TOPNL.getSoort().equalsIgnoreCase(this.config.getSoort())){
                List<Long> ids = new ArrayList<>();
                for (String type : TopNLDirectoryScanner.subdirectoryNames) {
                    ids.addAll(Arrays.asList(brmo.getLaadProcessenIds("bestand_datum", "ASC", type, "STAGING_OK")));
                }
                lpIds = ArrayUtils.toPrimitive(ids.toArray(new Long[ids.size()]));
            }else{
                lpIds = ArrayUtils.toPrimitive(brmo.getLaadProcessenIds("bestand_datum", "ASC", config.getSoort(), "STAGING_OK"));
            }
            brmo.setOrderBerichten(!config.alsStandTransformeren());
            if (lpIds == null || lpIds.length == 0) {
                msg = "Er zijn geen laadprocessen van soort " + config.getSoort() + " en status: 'STAGING_OK' om te transformeren.";
                LOG.info(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                listener.addLog(msg);
            } else {
                listener.updateStatus("Transformeren...");
                Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, lpIds,
                        new nl.b3p.brmo.loader.ProgressUpdateListener() {
                    @Override
                    public void total(long total) {
                        sb.append("Totaal te transformeren: ")
                                .append(total).append(AutomatischProces.LOG_NEWLINE);
                        listener.total(total);
                    }

                    @Override
                    public void progress(long progress) {
                        listener.progress(progress);
                    }

                    @Override
                    public void exception(Throwable t) {
                        sb.append("Fout tijdens laadproces transformeren: ")
                                .append(t.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
                        listener.exception(t);
                    }
                }
                );
                t.join();
            }

            if (transformErrorOccured) {
                this.config.setStatus(ERROR);
                msg = "Handmatige transformatie vanuit de laadprocessen pagina is noodzakelijk.";
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                LOG.warn(msg);
                msg = String.format("Laadproces transformatie proces met ID %d is niet succesvol afgerond op %tc.", config.getId(), Calendar.getInstance());
                LOG.error(msg);
                listener.updateStatus("Er is een fout opgetreden.");
            } else {
                this.config.setStatus(WAITING);
                msg = String.format("Laadproces transformatie proces met ID %d is succesvol afgerond op %tc.", config.getId(), Calendar.getInstance());
                LOG.info(msg);
                listener.updateStatus("Transformatie afgerond.");
            }

            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            this.listener.addLog(msg);
            this.config.setSamenvatting(msg);
            this.config.setLogfile(sb.toString());
        } catch (BrmoException | InterruptedException e) {
            LOG.error("Fout bij transformeren laadprocessen naar RSGB", e);
            String m = "Fout bij transformeren laadprocessen naar RSGB: " + ExceptionUtils.getMessage(e);
            if (e.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(e);
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
            Stripersist.getEntityManager().getTransaction().commit();
            Stripersist.getEntityManager().clear();
        }
    }

}
