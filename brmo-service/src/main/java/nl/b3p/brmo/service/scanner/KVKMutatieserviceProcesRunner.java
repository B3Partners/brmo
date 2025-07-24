package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces;

public class KVKMutatieserviceProcesRunner extends AbstractExecutableProces{

    private final KVKMutatieserviceProces config;

    public KVKMutatieserviceProcesRunner(KVKMutatieserviceProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {

    }

    @Override
    public void execute(ProgressUpdateListener listener) {

    }
}
