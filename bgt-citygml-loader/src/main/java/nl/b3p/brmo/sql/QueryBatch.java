package nl.b3p.brmo.sql;

import java.sql.SQLException;

public interface QueryBatch {
    boolean addBatch(Object[] params) throws Exception;

    void executeBatch() throws Exception;

    void close() throws SQLException;
}
