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
    
    private boolean deleteData = false;
    
    /* Used for comfort metadata tables */
    private String comfortSnapshotDate;
    
    List<TableRow> rows = new ArrayList<TableRow>();
    
    public TableData(TableRow row) {
        this.deleteData = false;
        this.comfortData = false;  
        this.rows.add(row);
    }
    
    public TableData(String comfortSearchTable, String comfortSearchColumn, 
            String comfortSearchValue, String comfortSnapshotDate) {
        
        this.comfortData = true;
        this.deleteData = false;
        this.comfortSearchTable = comfortSearchTable;
        this.comfortSearchColumn = comfortSearchColumn;
        this.comfortSearchValue = comfortSearchValue;    
        this.comfortSnapshotDate = comfortSnapshotDate;
    }

    public TableData() {
        this.deleteData = true;
        this.comfortData = false;
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
    
    public boolean isDeleteData() {
        return deleteData;
    }

    public void setDeleteData(boolean deleteData) {
        this.deleteData = deleteData;
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
