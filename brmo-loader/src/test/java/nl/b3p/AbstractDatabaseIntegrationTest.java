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
    protected final Properties params = new Properties();
    protected boolean isOracle;
    protected boolean isMsSQL;

    @Rule
    public TestName name = new TestName();

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
    }

    /**
     * subklassen dienen zelf ook een setup te hebben.
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

        try {
            Class rsgbDriverClass = Class.forName(params.getProperty("rsgb.jdbc.driverClassName"));
            Class stagingDriverClass = Class.forName(params.getProperty("staging.jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.warn("database driver niet gevonden.", ex);
        }
    }

    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }

}
