package nl.b3p.brmo.loader.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Matthijs Laan
 */
public class TableRow {
    private String table;
    private boolean ignoreDuplicates;

    private List<String> columns = new ArrayList();
    private List<String> values = new ArrayList();

    private final Set<String> alleenArchiefColumns = new HashSet();

    /* Used for archief tables */
    private String columnDatumBeginGeldigheid;
    private String columnDatumEindeGeldigheid;
    
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public boolean isAlleenArchiefColumn(String column) {
        return alleenArchiefColumns.contains(column);
    }

    public void setAlleenArchiefColumn(String column) {
        alleenArchiefColumns.add(column);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public boolean isIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public void setIgnoreDuplicates(boolean ignoreDuplicates) {
        this.ignoreDuplicates = ignoreDuplicates;
    }

    public String getColumnDatumBeginGeldigheid() {
        return columnDatumBeginGeldigheid;
    }

    public void setColumnDatumBeginGeldigheid(String columnDatumBeginGeldigheid) {
        this.columnDatumBeginGeldigheid = columnDatumBeginGeldigheid;
    }

    public String getColumnDatumEindeGeldigheid() {
        return columnDatumEindeGeldigheid;
    }

    public void setColumnDatumEindeGeldigheid(String columnDatumEindeGeldigheid) {
        this.columnDatumEindeGeldigheid = columnDatumEindeGeldigheid;
    }
 
    public String toString(List<String> displayColumns) {
        StringBuilder s = new StringBuilder();
        s.append(table).append("[");

        List<String> keys = new ArrayList();
        int i = -1;
        for (String column: columns) {
            i++;
            if(!displayColumns.contains(column)) {
                continue;
            }

            keys.add(column + "=" + values.get(i));
        }
        s.append(StringUtils.join(keys, ", "));
        s.append("]");
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("TABLE: ");
        str.append(table);
        str.append("\n");

        int i = 0;
        for (String column : columns) {
            str.append(column);
            str.append("=");
            str.append(values.get(i));
            if(isAlleenArchiefColumn(column)) {
                str.append(" (alleen archief)");
            }
            str.append("\n");
            i++;
        }

        return str.toString();
    }

    public String getColumnValue(String column) {
        int i = columns.indexOf(column);
        if(i == -1) {
            return null;
        } else {
            return values.get(i);
        }
    }
}