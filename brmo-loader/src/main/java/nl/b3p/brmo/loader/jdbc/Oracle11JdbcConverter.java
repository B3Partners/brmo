/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.sql.SQLException;
import oracle.jdbc.OracleConnection;

/**
 * Oracle 11g specifieke overrides.
 *
 * @author mprins
 */
public class Oracle11JdbcConverter extends OracleJdbcConverter {

    public Oracle11JdbcConverter(OracleConnection oc) throws SQLException {
        super(oc);
    }

    /**
     * Oracle 11 specifieke versie om aantal rijen van een insert/select te
     * beperken. Maakt gebruik van de ROWNUM functie omdat
     * {@code FETCH FIRST ... ROWS ONLY} alleen in Oracle 12 werkt. Zie ook:
     * <a href="https://github.com/B3Partners/brmo/issues/294">GH 294</a>.
     *
     *
     * @param sql query zonder limiet
     * @param limit max aantal op te halen records dat voldoet aan query
     * @return query met limiet
     * @since 1.4.3
     * @see OracleJdbcConverter#buildLimitSql(java.lang.StringBuilder, int)
     */
    @Override
    public StringBuilder buildLimitSql(StringBuilder sql, int limit) {
        sql.append(" AND ROWNUM <= ").append(limit);
        return sql;
    }
}
