/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import static org.junit.Assume.assumeNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Utility om database properties te laden en methods te loggen.
 *
 * @author mprins
 */
public abstract class AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(AbstractDatabaseIntegrationTest.class);

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
    }

    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #loadProps()
     */
    protected final Properties params = new Properties();

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

    /**
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

    /**
     * subklassen dienen zelf een setup te hebben.
     *
     * @throws Exception if any
     */
    @Before
    abstract public void setUp() throws Exception;

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    public void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(AbstractDatabaseIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(AbstractDatabaseIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
        isMsSQL = "jtds-sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));
        isPostgis = "postgis".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class rsgbDriverClass = Class.forName(params.getProperty("rsgb.jdbc.driverClassName"));
            Class stagingDriverClass = Class.forName(params.getProperty("staging.jdbc.driverClassName"));
            Class rsgbbgtDriverClass = Class.forName(params.getProperty("rsgbbgt.jdbc.driverClassName"));
            Class topnlDriverClass = Class.forName(params.getProperty("topnl.jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }

    /**
     * Log de naam van de test als deze begint.
     */
    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }

}
