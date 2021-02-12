package nl.b3p.brmo.datamodel;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.*;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public abstract class P8TestFramework {
    private static final Log LOG = LogFactory.getLog(P8TestFramework.class);
    /**
     * properties uit {@code P8.properties} en
     * {@code local.P8.properties}.
     *
     * @see #loadProps()
     */
    protected static final Properties params = new Properties();
    /**
     * our test client.
     */
    protected static CloseableHttpClient client;

    /**
     * voor dbunit.
     */
    protected IDatabaseConnection rsgb;
    /**
     * voor jdbc interactie.
     */
    protected BasicDataSource dsRsgb;

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    @AfterEach
    public abstract void cleanup() throws Exception;

    @BeforeEach
    public abstract void setup() throws Exception;

    /**
     * initialize http client aan de hand van de aangegeven properties.
     */
    @BeforeAll
    public static void setUpClass() throws IOException {
        //assumeNotNull("Verwacht P8 properties te zijn aangegeven.", System.getProperty("p8.properties.file"));

        loadProps();

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                params.getProperty("p8.username"),
                params.getProperty("p8.password")
        );
        provider.setCredentials(AuthScope.ANY, credentials);

        client = HttpClients.custom()
                .useSystemProperties()
                .setUserAgent("brmo P8 integration test")
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    /**
     * close http client connections.
     *
     * @throws IOException if any occurs closing the http connection
     */
    @AfterAll
    public static void tearDownClass() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Laadt de database property file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    private static void loadProps() throws IOException {
        params.load(P8ServicesIntegrationTest.class.getClassLoader().getResourceAsStream("P8.properties"));

        LOG.debug("basis parameters: " + params);

        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(P8ServicesIntegrationTest.class.getClassLoader().getResourceAsStream("local.P8.properties"));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand -met wachtwoorden- is normaal niet aanwezig, alleen lokaal voor ontwikkelaars
            LOG.debug("geen extra parameters geladen.");
        }
        try {
            Class.forName(params.getProperty("rsgb.jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }

    public void setUpDB() throws SQLException {
        dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.username"));
        dsRsgb.setPassword(params.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    }
}
