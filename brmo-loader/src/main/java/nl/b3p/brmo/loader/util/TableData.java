package nl.b3p.brmo.loader.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Boy de Wit
 */
public class TableData {
    
    private boolean comfortData = false;
    private String comfortSearchTable;
    private String comfortSearchColumn;
    private String comfortSearchValue;
    
    /* Used for comfort metadata tables */
    private String comfortSnapshotDate;
    
    List<TableRow> rows = new ArrayList<TableRow>();
    
    public TableData(TableRow row) {
        this.comfortData = false;  
        this.rows.add(row);
    }
    
    public TableData(String comfortSearchTable, String comfortSearchColumn, 
            String comfortSearchValue, String comfortSnapshotDate) {
        
        this.comfortData = true;
        this.comfortSearchTable = comfortSearchTable;
        this.comfortSearchColumn = comfortSearchColumn;
        this.comfortSearchValue = comfortSearchValue;    
        this.comfortSnapshotDate = comfortSnapshotDate;
    }
    
    public void addRow(TableRow row) {
        rows.add(row);      
    }

    public boolean isComfortData() {
        return comfortData;
    }

    public void setComfortData(boolean comfortData) {
        this.comfortData = comfortData;
    }

    public String getComfortSearchTable() {
        return comfortSearchTable;
    }

    public void setComfortSearchTable(String comfortSearchTable) {
        this.comfortSearchTable = comfortSearchTable;
    }

    public String getComfortSearchColumn() {
        return comfortSearchColumn;
    }

    public void setComfortSearchColumn(String comfortSearchColumn) {
        this.comfortSearchColumn = comfortSearchColumn;
    }

    public String getComfortSearchValue() {
        return comfortSearchValue;
    }

    public void setComfortSearchValue(String comfortSearchValue) {
        this.comfortSearchValue = comfortSearchValue;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }

    public String getComfortSnapshotDate() {
        return comfortSnapshotDate;
    }

    public void setComfortSnapshotDate(String comfortSnapshotDate) {
        this.comfortSnapshotDate = comfortSnapshotDate;
    }
}
