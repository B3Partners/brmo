package nl.b3p.brmo.loader.pipeline;

import nl.b3p.brmo.loader.BerichtenHandler;

/**
 *
 * @author Matthijs Laan
 */
public class UpdateResultPipeline extends BerichtPipelineThread {

    public UpdateResultPipeline(BerichtenHandler handler, int capacity, String stopwatchPrefix) {
        super(handler, capacity, stopwatchPrefix + ".updateresult");
    }

    @Override
    void work(BerichtWorkUnit workUnit) throws Exception {
        handler.updateProcessingResult(workUnit.getBericht());
    }
}
