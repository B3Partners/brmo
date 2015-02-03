package nl.b3p.brmo.loader.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import nl.b3p.brmo.loader.BerichtenHandler;
import nl.b3p.brmo.loader.pipeline.BerichtWorkUnit;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class ProcessDbXmlPipeline extends Thread {
    private static final Log log = LogFactory.getLog(ProcessDbXmlPipeline.class);

    private final BlockingQueue<BerichtWorkUnit> queue;
    private final BerichtenHandler handler;

    private boolean stopWhenQueueEmpty = false;
    private boolean abort = false;

    public ProcessDbXmlPipeline(BlockingQueue<BerichtWorkUnit> queue, BerichtenHandler handler) {
        this.queue = queue;
        this.handler = handler;
    }

    public void stopWhenQueueEmpty() {
        this.stopWhenQueueEmpty = true;
    }

    public void setAbortFlag() {
        this.abort = true;
    }

    @Override
    public void run() {
        log.info("started, waiting for transformed berichten");

        while(true) {
            BerichtWorkUnit workUnit = null;
            try {
                workUnit = queue.poll(1, TimeUnit.SECONDS);
            } catch(InterruptedException e) {
                log.info("interrupted while polling queue");
            }

            if(abort) {
                log.info("aborting!");
                return;
            }

            if(stopWhenQueueEmpty && workUnit == null) {
                log.info("queue empty, stopping");
                return;
            }
            if(workUnit == null) {
                continue;
            }
            log.info("handling bericht from queue (size " + queue.size() + ": " + workUnit.getBericht().getId() + ", " + workUnit.getBericht().getObjectRef());

            try {
                handler.handle(workUnit.getBericht(), workUnit.getTableData());
            } catch(BrmoException e) {
                // Do not log stacktrace, in database bericht.opmerking
                log.error("handle method returned exception (continuing): " + e.getClass() + ": " + e.getMessage());
            }
        }
    }
}
