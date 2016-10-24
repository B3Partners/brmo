/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

/**
 * Een ListHandler die een list van {@code Long} teruggeeft ipv
 * watdedatabaseverzint. Oracle geeft BigDecimal terug voor identity kolommen
 * van bijv. staging database tabellen omdat het NUMBER(19,0) kolommen zijn.
 *
 * @see org.apache.commons.dbutils.handlers.ColumnListHandler
 *
 * @author mprins
 */
public class LongColumnListHandler extends AbstractListHandler<Long> {

    /**
     * The column number to retrieve.
     */
    private final int columnIndex;

    /**
     * The column name to retrieve. Either columnName or columnIndex will be
     * used but never both.
     */
    private final String columnName;

    /**
     * Creates a new instance of ColumnListHandler. The first column of each row
     * will be returned from <code>handle()</code>.
     */
    public LongColumnListHandler() {
        this(1, null);
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnIndex The index of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    public LongColumnListHandler(int columnIndex) {
        this(columnIndex, null);
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnName The name of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    public LongColumnListHandler(String columnName) {
        this(1, columnName);
    }

    /**
     * Private Helper
     *
     * @param columnIndex The index of the column to retrieve from the
     * <code>ResultSet</code>.
     * @param columnName The name of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    private LongColumnListHandler(int columnIndex, String columnName) {
        super();
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    /**
     * Returns one <code>ResultSet</code> column value as <code>Long</code>.
     *
     * @param rs <code>ResultSet</code> to process.
     * @return <code>Object</code>, never <code>null</code>.
     *
     * @throws SQLException if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the
     * column type
     *
     * @see
     * org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Long handleRow(ResultSet rs) throws SQLException {
        Object o;
        Long l;
        if (this.columnName == null) {
            o = rs.getObject(this.columnIndex);
        } else {
            o = rs.getObject(this.columnName);
        }
        if (o instanceof BigDecimal) {
            l = ((BigDecimal) o).longValue();
        } else {
            l = (Long) o;
        }
        return l;
    }
}
