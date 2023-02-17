/*
 * Copyright (C) 2022 B3Partners B.V.
 */
package nl.b3p.brmo.test.util.database.dbunit;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.operation.DatabaseOperation;

public class DropAllTablesOperation extends DatabaseOperation {
  private static final Log LOG = LogFactory.getLog(DropAllTablesOperation.class);
  private final String DROP_PGSQL = "drop table %s cascade";
  private final String DROP_ORACLE = "drop table %s cascade purge";

  /**
   * Executes this operation on the specified database using the specified dataset contents.
   *
   * @param connection the database connection.
   * @param dataSet the dataset to be used by this operation.
   */
  @Override
  public void execute(IDatabaseConnection connection, IDataSet dataSet)
      throws DatabaseUnitException, SQLException {
    // get ordered list of tables
    ITableFilter filter = new DatabaseSequenceFilter(connection);
    final String dropStmt = connection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY).getClass().getName().contains("Oracle")? DROP_ORACLE : DROP_PGSQL;
    IDataSet dataset;
    if (null == dataSet) {
      dataset = new FilteredDataSet(filter, connection.createDataSet());
    } else {
      dataset = new FilteredDataSet(filter, dataSet);
    }
    LOG.debug("to be dropped table names: " + Arrays.toString(dataset.getTableNames()));
    String[] tableNames = dataset.getTableNames();
    List<String> reversedTableNames = Arrays.asList(tableNames);
    Collections.reverse(reversedTableNames);
    LOG.debug("to be dropped table names in order: " + Arrays.toString(dataset.getTableNames()));
    try (Statement stmt = connection.getConnection().createStatement()) {
      for (String table : reversedTableNames) {
        stmt.addBatch(String.format(dropStmt, table));
      }
      stmt.executeBatch();
    }
  }
}
