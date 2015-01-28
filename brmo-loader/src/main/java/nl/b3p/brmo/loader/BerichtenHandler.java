
package nl.b3p.brmo.loader;

import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;

/**
 *
 * @author Matthijs Laan
 */
public interface BerichtenHandler {
    public void handle(Bericht b) throws BrmoException;
}
