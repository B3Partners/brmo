package nl.b3p.brmo.loader.pipeline;

import java.util.List;
import nl.b3p.brmo.loader.entity.Bericht;
import static nl.b3p.brmo.loader.pipeline.BerichtTypeOfWork.*;
import nl.b3p.brmo.loader.util.TableData;

/**
 *
 * @author Matthijs Laan
 */
public class BerichtWorkUnit {
    private BerichtTypeOfWork typeOfWork = TRANSFORM_TO_TABLEDATA;

    private Bericht bericht;

    private List<TableData> tableData;

    public BerichtWorkUnit(Bericht bericht) {
        this.bericht = bericht;
    }

    public BerichtTypeOfWork getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(BerichtTypeOfWork typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public Bericht getBericht() {
        return bericht;
    }

    public void setBericht(Bericht bericht) {
        this.bericht = bericht;
    }

    public List<TableData> getTableData() {
        return tableData;
    }

    public void setTableData(List<TableData> tableData) {
        this.tableData = tableData;
    }

}
