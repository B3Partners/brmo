/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.testutil;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 *
 * @author mprins
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class TestUtil {

    protected static boolean haveSetupJNDI = false;

    private static final Log LOG = LogFactory.getLog(TestUtil.class);
    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #loadDBprop()
     */
    protected final Properties DBPROPS = new Properties();

    /**
     * {@code true} als we met een Oracle database bezig zijn.
     */
    protected boolean isOracle;

    /**
     * {@code true} als we met een MS SQL Server database bezig zijn.
     */
    protected boolean isMsSQL;

    /**
     * {@code true} als we met een Postgis database bezig zijn.
     */
    protected boolean isPostgis;

    protected BasicDataSource dsStaging;
    protected BasicDataSource dsRsgb;
    protected BasicDataSource dsRsgbBgt;
    protected BasicDataSource dsTopnl;

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        assumeFalse(getProperty("database.properties.file") == null, "Verwacht database omgeving te zijn aangegeven.");
    }

    /**
     * subklassen dienen zelf een setup te hebben; vanwege de overerving gaat
     * deze methode af na de {@code @Before} methoden van de superklasse, bijv.
     * {@link #loadDBprop()}.
     *
     * @throws Exception if any
     */
    @BeforeEach
    abstract public void setUp() throws Exception;

    /**
     * initialize database props using the environment provided file.
     *
     * @throws java.io.IOException if loading the property file fails
     */
    @BeforeEach
    public void loadDBprop() throws IOException {
        // the `database.properties.file`  is set in the pom.xml or using the commandline
        DBPROPS.load(TestUtil.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // see if a local version exists and use that to override
            DBPROPS.load(TestUtil.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // ignore this
        }

        isOracle = "oracle".equalsIgnoreCase(DBPROPS.getProperty("dbtype"));
        isMsSQL = "sqlserver".equalsIgnoreCase(DBPROPS.getProperty("dbtype"));
        isPostgis = "postgis".equalsIgnoreCase(DBPROPS.getProperty("dbtype"));

        try {
            Class.forName(DBPROPS.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }

        dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);
        dsStaging.setInitialSize(1);
        dsStaging.setMaxActive(20);
        dsStaging.setMaxIdle(1);
        dsStaging.setPoolPreparedStatements(true);

        dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
        dsRsgb.setInitialSize(1);
        dsRsgb.setMaxActive(20);
        dsRsgb.setMaxIdle(1);
        dsRsgb.setPoolPreparedStatements(true);

        dsRsgbBgt = new BasicDataSource();
        dsRsgbBgt.setUrl(DBPROPS.getProperty("rsgbbgt.url"));
        dsRsgbBgt.setUsername(DBPROPS.getProperty("rsgbbgt.username"));
        dsRsgbBgt.setPassword(DBPROPS.getProperty("rsgbbgt.password"));
        dsRsgbBgt.setAccessToUnderlyingConnectionAllowed(true);
        dsRsgbBgt.setInitialSize(1);
        dsRsgbBgt.setMaxActive(20);
        dsRsgbBgt.setMaxIdle(1);

        dsTopnl= new BasicDataSource();
        dsTopnl.setUrl(DBPROPS.getProperty("topnl.url"));
        dsTopnl.setUsername(DBPROPS.getProperty("topnl.username"));
        dsTopnl.setPassword(DBPROPS.getProperty("topnl.password"));
        dsTopnl.setAccessToUnderlyingConnectionAllowed(true);
        dsTopnl.setInitialSize(1);
        dsTopnl.setMaxActive(20);
        dsTopnl.setMaxIdle(1);

        setupJNDI();
    }

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    @AfterAll
    public void closeConnections() throws SQLException {
        // JNDI connectie pools wel sluiten! een ds kan null zijn als subklasse de #loadDBprop override
        if (dsStaging != null) {
            dsStaging.close();
        }
        if (dsRsgb != null) {
            dsRsgb.close();
        }
        if (dsRsgbBgt != null) {
            dsRsgbBgt.close();
        }
        if (dsTopnl != null) {
            dsTopnl.close();
        }
        haveSetupJNDI = false;
    }

    /**
     * setup jndi voor testcases.
     */
    protected void setupJNDI() {
        if (!haveSetupJNDI) {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            InitialContext ic;
            try {
                ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:comp");
                ic.createSubcontext("java:comp/env");
                ic.createSubcontext("java:comp/env/jdbc");
                ic.createSubcontext("java:comp/env/jdbc/brmo");
                ic.bind("java:comp/env/jdbc/brmo/rsgb", dsRsgb);
                ic.bind("java:comp/env/jdbc/brmo/staging", dsStaging);
                ic.bind("java:comp/env/jdbc/brmo/rsgbbgt", dsRsgbBgt);
                ic.bind("java:comp/env/jdbc/brmo/topnl", dsTopnl);
            } catch (NamingException ex) {
                LOG.warn("Opzetten van datasource jndi is mislukt: " + ex.getLocalizedMessage());
                LOG.trace("Opzetten van datasource jndi is mislukt:", ex);
            } finally {
                haveSetupJNDI = true;
            }
        }
    }
}
