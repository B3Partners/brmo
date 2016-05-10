package nl.b3p.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 *
 * @author Mark Prins
 */
public abstract class TestUtil {
    private static final Log LOG = LogFactory.getLog(TestUtil.class);

    /**
     * the server root url. {@value}
     */
    public static final String BASE_TEST_URL = "http://localhost:9090/brmo-service/";
    /**
     * This has the database properties as defined in 'postgres.properties' or
     * sqlserver.properties.
     *
     * @see #loadDBprop()
     */
    protected static final Properties DBPROPS = new Properties();

    /**
     * our test client.
     */
    protected static CloseableHttpClient client;

    /**
     * initialize database props using the environment provided file.
     *
     * @throws java.io.IOException if loading the property file fails
     */
    @BeforeClass
    public static void loadDBprop() throws IOException {
        // the `database.properties.file`  is set in the pom.xml or using the commandline
        DBPROPS.load(IndexPageIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // see if a local version exists and use that to override
            DBPROPS.load(IndexPageIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // ignore this 
        }
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
    @Rule
    public TestName name = new TestName();

    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }
}
