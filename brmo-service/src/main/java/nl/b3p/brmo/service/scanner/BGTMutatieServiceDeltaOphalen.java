package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.BGTMutatieServiceDeltaProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Transient;

public class BGTMutatieServiceDeltaOphalen extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(BGTMutatieServiceDeltaOphalen.class);

    private final BGTMutatieServiceDeltaProces config;

    @Transient
    private ProgressUpdateListener listener;

    public BGTMutatieServiceDeltaOphalen(BGTMutatieServiceDeltaProces config) {
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
        this.listener = listener;
    }
}
