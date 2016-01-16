/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.util.Date;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
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
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BerichtTransformatieUitvoeren extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BerichtTransformatieUitvoeren.class);

    private final BerichtTransformatieProces config;

    private ProgressUpdateListener l;

    public BerichtTransformatieUitvoeren(BerichtTransformatieProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            @Override
            public void total(long total) {
                config.addLogLine("Totaal aantal te transformeren berichten: " + total);
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                config.addLogLine("FOUT: " + t.getLocalizedMessage());
                log.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
                config.addLogLine(log);
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.l = listener;

        l.updateStatus("Initialiseren...");
        l.addLog(String.format("Initialiseren... %tc", new Date()));
        this.config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
        Stripersist.getEntityManager().flush();

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
                    l.total(total);
                }

                @Override
                public void progress(long progress) {
                    l.progress(progress);
                }

                @Override
                public void exception(Throwable t) {
                    l.exception(t);
                }
            });
            t.join();

            this.config.setStatus(AutomatischProces.ProcessingStatus.WAITING);
            this.config.setSamenvatting("Bericht transformatie is afgerond.");
            this.config.setLastrun(new Date());
            l.addLog("Bericht transformatie is afgerond.");
        } catch (Throwable t) {
            log.error("Fout bij transformeren berichten naar RSGB", t);
            String m = "Fout bij transformeren berichten naar RSGB: " + ExceptionUtils.getMessage(t);
            if (t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            this.config.updateSamenvattingEnLogfile(m);
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            this.config.setLastrun(new Date());
        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().flush();
        }
    }
}
