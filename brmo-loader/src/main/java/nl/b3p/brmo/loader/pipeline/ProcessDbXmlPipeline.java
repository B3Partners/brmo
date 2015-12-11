package nl.b3p.brmo.loader.pipeline;

import nl.b3p.brmo.loader.BerichtenHandler;

/**
 *
 * @author Matthijs Laan
 */
public class ProcessDbXmlPipeline extends BerichtPipelineThread {

    public ProcessDbXmlPipeline(BerichtenHandler handler, int capacity, String stopwatchPrefix) {
        super(handler, capacity, stopwatchPrefix + ".processdbxml");
    }

    @Override
    void work(BerichtWorkUnit workUnit) throws Exception {
        handler.handle(workUnit.getBericht(), workUnit.getTableData(), true);
    }
}
