
package nl.b3p.brmo.loader;

import java.util.List;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.TableData;

/**
 *
 * @author Matthijs Laan
 */
public interface BerichtenHandler {
    public void handle(Bericht b, List<TableData> pretransformedTableData) throws BrmoException;
    public List<TableData> transformToTableData(Bericht ber) throws BrmoException;
}
