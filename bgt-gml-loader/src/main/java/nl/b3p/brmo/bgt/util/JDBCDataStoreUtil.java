/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.bgt.util;

import java.lang.reflect.Method;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.geotools.util.factory.Hints;
import org.geotools.jdbc.JDBCDataStore;

/**
 *
 * @author matthijsln
 */
public class JDBCDataStoreUtil {
    public static boolean truncateTable(JDBCDataStore dataStore, String typeName, boolean isOracle, Log log) {
        log.info("Probeer tabel leeg te maken: " + typeName);

        try {
            StringBuffer sql = new StringBuffer("TRUNCATE TABLE ");
            Method encodeTableName = dataStore.getClass().getDeclaredMethod("encodeTableName", String.class, StringBuffer.class, Hints.class);
            encodeTableName.setAccessible(true);
            encodeTableName.invoke(dataStore, typeName, sql, null);

            if(isOracle) {
                sql.append(" PURGE MATERIALIZED VIEW LOG REUSE STORAGE");
            }
            log.trace("SQL: " + sql.toString());
            new QueryRunner(dataStore.getDataSource()).update(sql.toString());
            log.info("Tabel leeggemaakt");
            return true;
        } catch(Exception e) {
            log.debug("Exception bij TRUNCATE", e);
            log.warn("Fout bij TRUNCATE, probeer met DELETE FROM: " + e.getClass() + ": " + e.getMessage());

            try {
                StringBuffer sql = new StringBuffer("DELETE FROM ");
                Method encodeTableName = dataStore.getClass().getDeclaredMethod("encodeTableName", String.class, StringBuffer.class, Hints.class);
                encodeTableName.setAccessible(true);
                encodeTableName.invoke(dataStore, typeName, sql, null);
                log.trace("SQL: " + sql.toString());
                new QueryRunner(dataStore.getDataSource()).update(sql.toString());
                log.info("Tabel leeggemaakt met DELETE FROM");
                return true;
            } catch(Exception e2) {
                log.error("Fout bij DELETE FROM, tabel kon niet leeggemaakt worden! ", e2);
                return false;
            }
        }
    }
}
