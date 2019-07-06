package nl.b3p.brmo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static java.lang.System.getProperty;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

public class OracleDriverTest {
    private static final Log LOG = LogFactory.getLog(OracleDriverTest.class);
    private final String dbName = "staging";
    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #setUpProps()
     */
    private final Properties params = new Properties();
    /**
     * {@code true} als we met een Oracle database bezig zijn.
     */
    private boolean isOracle;

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", getProperty("database.properties.file"));
    }

    @Before
    public void setUpProps() {
        try {
            // de `database.properties.file` is in de pom.xml of via commandline ingesteld
            params.load(OracleDriverTest.class.getClassLoader()
                    .getResourceAsStream(getProperty("database.properties.file")));
            // probeer een local (override) versie te laden als die bestaat
            params.load(OracleDriverTest.class.getClassLoader()
                    .getResourceAsStream("local." + getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class stagingDriverClass = Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }


    @Test
    public void testOracleDriverImpl() throws SQLException {
        params.getProperty("test");
        if (isOracle) {
            // testcase uit https://github.com/B3Partners/brmo/issues/89
            String prepSQL = "select * from (select /*+ FIRST_ROWS(n) */ a.*, ROWNUM rnum from (select * from bericht where job_id = ? order by soort, object_ref, datum, volgordenummer ) a where ROWNUM <=250 ) where rnum  > 0";

            try (Connection conn = DriverManager.getConnection(
                    params.getProperty(this.dbName + ".url"),
                    params.getProperty(this.dbName + ".username"),
                    params.getProperty(this.dbName + ".password"))) {
                try {
                    PreparedStatement pstmt = conn.prepareStatement(prepSQL);
                    int stmtCount = pstmt.getParameterMetaData().getParameterCount();
                    LOG.debug("Poging met getParameterMetaData, stmtCount=" + stmtCount);
                    pstmt.setLong(1, 1444897737715L);
                    ResultSet prset = pstmt.executeQuery();
                    while (prset.next()) {
                        LOG.debug(prset.getString(1));
                    }
                    assertTrue(true);
                } catch (Exception e) {
                    LOG.error(e);
                    fail(e.getLocalizedMessage());
                }
                try {
                    PreparedStatement pstmt = conn.prepareStatement(prepSQL);
                    LOG.debug("Poging zonder getParameterMetaData");
                    pstmt.setLong(1, 1444897737715L);
                    ResultSet prset = pstmt.executeQuery();
                    while (prset.next()) {
                        LOG.debug(prset.getString(1));
                    }
                    assertTrue(true);
                } catch (Exception e) {
                    LOG.error(e);
                    fail(e.getLocalizedMessage());
                }
            }
        }
    }
}
