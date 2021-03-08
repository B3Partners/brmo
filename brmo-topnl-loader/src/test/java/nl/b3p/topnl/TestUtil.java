/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.topnl;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 * @author mprins
 */
public class TestUtil {
    
    private final static Log LOG = LogFactory.getLog(TestUtil.class);
    protected DataSource datasource;
    protected boolean useDB = false;
    
    protected QueryRunner run;

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    @BeforeEach
    public void setUpClass(TestInfo testInfo) throws SQLException, IOException {
        if(useDB){
            JDBCDataSource ds = new JDBCDataSource();
            String testname = testInfo.getDisplayName().replace(':','-').replace(' ','_');
            ds.setUrl("jdbc:hsqldb:file:./target/unittest-hsqldb_" + testname + "_" + System.currentTimeMillis() + "/db;shutdown=true");
            datasource = ds;
            initDB("initdb250nl.sql");
            initDB("initdb100nl.sql");
            initDB("initdb50nl.sql");
            initDB("initdb10nl.sql");
            GeometryJdbcConverter gjc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection());
            run = new QueryRunner(datasource, gjc.isPmdKnownBroken() );
        }
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }
    
    private void initDB(String file) throws IOException{
        try {
            Reader f = new InputStreamReader(TestUtil.class.getResourceAsStream(file));
            executeScript(f);
        } catch (SQLException sqle) {
            LOG.error("Error initializing testdb:", sqle);
        }

    }
    
    
    public void executeScript(Reader f) throws IOException, SQLException {

        try (Connection conn = datasource.getConnection()) {
            ScriptRunner sr = new ScriptRunner(conn, true, true);
            sr.runScript(f);
            conn.commit();
        }
    }
    
}
