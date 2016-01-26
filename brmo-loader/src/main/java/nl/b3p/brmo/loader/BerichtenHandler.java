
package nl.b3p.brmo.loader;

import java.sql.SQLException;
import java.util.List;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.TableData;

/**
 *
 * @author Matthijs Laan
 */
public interface BerichtenHandler {
    public void updateBerichtProcessing(Bericht b) throws Exception;
    public List<TableData> transformToTableData(Bericht ber) throws BrmoException;
    public void handle(Bericht b, List<TableData> pretransformedTableData, boolean updateResult) throws BrmoException;
    public void updateProcessingResult(Bericht b);
    public void renewConnection() throws SQLException;
}
