/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.util.factory.GeoTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        assumeFalse(getProperty("database.properties.file") == null, "Verwacht database omgeving te zijn aangegeven.");
        GeoTools.init();
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
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    protected void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(TestingBase.class.getClassLoader()
                .getResourceAsStream(getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(TestingBase.class.getClassLoader()
                    .getResourceAsStream("local." + getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }

        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
        isMsSQL = "sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            fail("Laden van database driver (" + params.getProperty("jdbc.driverClassName") + ") is mislukt.");
        }
    }

    /**
     * Leeg bekende database tabellen.
     *
     * @see BGTGMLLightTransformerFactory#values()
     *
     * @throws Exception if any
     */
    protected void clearTables() throws Exception {
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
                    String sql = "DROP TABLE \"" + params.getProperty("schema") + "\".\"" + tableName + "\"";
                    LOG.trace("Droppen tabel: " + tableName + " met sql: " + sql);
                    try {
                        connection.createStatement().executeUpdate(sql);
                    } catch (SQLException se) {
                        LOG.warn("Mogelijke fout tijdens droppen van tabellen: " + se.getLocalizedMessage());
                    }
                }
                res.close();
            }
        }
    }

    protected SortedMap<String, Long> countRows() throws Exception {
        SortedMap<String, Long> counts = new TreeMap<>();
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
                    String sql = "SELECT COUNT(*) FROM \"" + params.getProperty("schema") + "\".\"" + tableName + "\"";
                    LOG.trace("count tabel: " + tableName + " met sql: " + sql);
                    try (ResultSet c = connection.createStatement().executeQuery(sql)) {
                        c.next();
                        counts.put(tableName, c.getLong(1));
                    }
                }
                res.close();
            }
        }
        for (Map.Entry<String, Long> count : counts.entrySet()) {
            LOG.info("tabel: '" + count.getKey() + "', aantal features geteld: " + count.getValue());
        }
        return counts;
    }
}
