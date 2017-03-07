/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.GeoTools;
import org.junit.After;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * utilities voor de integratie tests.
 *
 * @author mprins
 */
public abstract class TestingBase {

    private static final Log LOG = LogFactory.getLog(TestingBase.class);
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
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
        GeoTools.init();
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

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    protected void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(TestingBase.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(TestingBase.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
        isMsSQL = "jtds-sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));
    }

    /**
     * Leeg alle database tabellen.
     *
     * @throws Exception if any
     */
    protected void clearTables() throws Exception {
        try {
            Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            fail("Laden van database driver (" + params.getProperty("jdbc.driverClassName") + ") is mislukt.");
        }
        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))) {

            connection.setAutoCommit(true);

            String tableName;
            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                tableName = isOracle ? t.name().toUpperCase() : t.name();

                ResultSet res = connection.getMetaData().getTables(null, params.getProperty("schema"), tableName, new String[]{"TABLE"});
                if (res.next()) {
                    String sql = "DELETE FROM \"" + params.getProperty("schema") + "\".\"" + tableName + "\"";
                    try {
                        connection.createStatement().executeUpdate(sql);
                    } catch (SQLException se) {
                        LOG.warn("Mogelijke fout tijdens legen van tabellen: " + se.getLocalizedMessage());
                        LOG.debug(se);
                    }
                } else {
                    LOG.error("Verwacht een tabel " + tableName + " te hebben gevonden om te legen, maar tabel is niet gevonden.");
                }
                res.close();
            }
            connection.close();
        }
    }

    /**
     * Verwijder alle database tabellen.
     *
     * @throws Exception if any
     */
    protected void dropTables() throws Exception {
        try {
            Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            fail("Laden van database driver (" + params.getProperty("jdbc.driverClassName") + ") is mislukt.");
        }
        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))) {

            connection.setAutoCommit(true);

            String tableName;
            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                tableName = isOracle ? t.name().toUpperCase() : t.name();

                ResultSet res = connection.getMetaData().getTables(null, params.getProperty("schema"), tableName, new String[]{"TABLE"});
                if (res.next()) {
                    String sql = "DROP TABLE \"" + params.getProperty("schema") + "\".\"" + tableName + "\";";
                    LOG.info("Droppen tabel: " + tableName + " met sql: " + sql);
                    try {
                        connection.createStatement().executeUpdate(sql);
                    } catch (SQLException se) {
                        LOG.warn("Mogelijke fout tijdens droppen van tabellen: " + se.getLocalizedMessage());
                    }
                }
                res.close();
            }
            connection.close();
        }
    }
}
