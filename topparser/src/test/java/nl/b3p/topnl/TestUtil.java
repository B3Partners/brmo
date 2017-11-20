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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

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
    
    @Rule 
    public TestName testName = new TestName();

    /**
     * Log de naam van de test als deze begint.
     */
    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + testName.getMethodName());
    }

    @Before
    public void setUpClass() throws SQLException, IOException {
        if(useDB){
            JDBCDataSource ds = new JDBCDataSource();
            String testname = testName.getMethodName();
            long randomizer = System.currentTimeMillis();
            ds.setUrl("jdbc:hsqldb:file:." + File.separator + "target" + File.separator + "unittest-hsqldb" + File.separator + testname + "_" + randomizer + File.separator + "db;shutdown=true");
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
    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + testName.getMethodName());
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
        Connection conn = null;

        try {            
            conn = (Connection) datasource.getConnection();
            ScriptRunner sr = new ScriptRunner(conn, true, true);
            sr.runScript(f);
            conn.commit();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
}
