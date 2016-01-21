package nl.b3p.web;

import java.io.IOException;
import java.util.Properties;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Mark Prins
 */
public abstract class TestUtil {

    /**
     * the server root url. {@value}
     */
    public static final String BASE_TEST_URL = "http://localhost:9090/brmo-service/";
    /**
     * This has the database properties as defined in 'postgres.properties'.
     *
     * @see #loadDBprop()
     */
    protected static final Properties POSTGRESPROPS = new Properties();

    /**
     * our test client.
     */
    protected static CloseableHttpClient client;

    /**
     * initialize database props.
     *
     * @throws java.io.IOException if loading the property file fails
     */
    @BeforeClass
    public static void loadDBprop() throws IOException {
        POSTGRESPROPS.load(IndexPageIntegrationTest.class.getClassLoader().getResourceAsStream("postgres.properties"));
    }

    /**
     * initialize http client.
     */
    @BeforeClass
    public static void setUpClass() {
        client = HttpClients.custom()
                .useSystemProperties()
                .setUserAgent("brmo integration test")
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(new BasicCookieStore())
                .build();
    }

    /**
     * close http client connections.
     *
     * @throws IOException if any occurs closing the http connection
     */
    @AfterClass
    public static void tearDownClass() throws IOException {
        client.close();
    }
}
