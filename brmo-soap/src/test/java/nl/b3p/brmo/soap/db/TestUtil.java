/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.soap.db;

import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * @author mprins
 */
public abstract class TestUtil {

    private static final Log LOG = LogFactory.getLog(TestUtil.class);
    protected static boolean haveSetupJNDI = false;
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
    protected BasicDataSource dsRsgb;
    protected BasicDataSource dsStaging;

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        assumeFalse(getProperty("database.properties.file") == null, "Verwacht database omgeving te zijn aangegeven.");
    }

    /**
     * subklassen dienen zelf een setUp() te hebben; vanwege de overerving gaat
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
            Class driverClass = Class.forName(DBPROPS.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }

        dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        this.setupJNDI();
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


    /**
     * setup jndi voor testcases, doet 1 poging om jndi context te initialiseren.
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
            } catch (NamingException ex) {
                LOG.warn("Opzetten van datasource jndi is mislukt: " + ex.getLocalizedMessage());
                LOG.trace("Opzetten van datasource jndi is mislukt", ex);
            } finally {
                haveSetupJNDI = true;
            }
        }
    }

    /**
     * refresh de gegeven lijst van materialized views in de database.
     *
     * @param mviews lijst van views
     * @param ds     database koppeling
     * @throws SQLException if any
     */
    protected void refreshMViews(final String[] mviews, final BasicDataSource ds) throws SQLException {
        // refresh materialized views
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            for (String mview : mviews) {
                // "update" gebruiken omdat we een oracle stored procedure benaderen
                Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).update(conn, geomToJdbc.getMViewRefreshSQL(mview));
            }
        }
    }
}
