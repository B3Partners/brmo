package nl.b3p.brmo.service.scanner;

import javax.persistence.Transient;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.SimpleMessage;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BerichtTransformatieProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BerichtTransformatieUitvoeren extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(GDS2OphalenProces.class);

    private final BerichtTransformatieProces config;

    private ProgressUpdateListener l;

    public BerichtTransformatieUitvoeren(BerichtTransformatieProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            /* een doet bijna niks listener */
            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
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
        this.config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
        Stripersist.getEntityManager().flush();

        BrmoFramework brmo = null;
        try {
            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            DataSource dataSourceRsgb = ConfigUtil.getDataSourceRsgb();
            brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);
            brmo.setEnablePipeline(true);
            brmo.setTransformPipelineCapacity(100);

            Thread t = brmo.toRsgb((nl.b3p.brmo.loader.ProgressUpdateListener) this.l);
            t.join();

            this.config.setStatus(AutomatischProces.ProcessingStatus.WAITING);

        } catch (Throwable t) {
            log.error("Fout bij transformeren berichten naar RSGB", t);
            String m = "Fout bij transformeren berichten naar RSGB: " + ExceptionUtils.getMessage(t);
            if (t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            this.config.addLogLine(m);
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);

        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
            
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().getTransaction().commit();
        }

    }
}
