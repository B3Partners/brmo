
package nl.b3p.brmo.loader;

/**
 *
 * @author Matthijs Laan
 */
public interface ProgressUpdateListener {

    public void total(long total);
    public void progress(long progress);
    public void exception(Throwable t);
}
