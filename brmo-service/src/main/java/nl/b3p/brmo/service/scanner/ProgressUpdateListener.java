
package nl.b3p.brmo.service.scanner;

/**
 *
 * @author Matthijs Laan
 */
public interface ProgressUpdateListener {

    public void total(long total);
    public void progress(long progress);
    public void exception(Throwable t);
    public void updateStatus(String status);
    public void addLog(String log);
}
