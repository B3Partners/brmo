package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 *
 * @author Matthijs Laan
 */

@Entity
public class GDS2OphaalProces extends AutomatischProces {

    @Override
    public void execute(ProgressUpdateListener listener) {
        listener.updateStatus("Starting...");
        try {
            Thread.sleep(4000);
            listener.updateStatus("Executing...");
            listener.progress(50);
            Thread.sleep(4000);
            listener.updateStatus("Finished...");
            listener.progress(100);
        } catch (InterruptedException ex) {
        }
    }
}
