package nl.b3p.web;

import java.io.IOException;
import java.util.Properties;
import nl.b3p.brmo.service.testutil.TestUtil;
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
 * Integratie test utility klasse om online integratie tests te bouwen welke
 * tegen een brmo-service kunnen worden uitgevoerd.
 *
 * @author Mark Prins
 */
public abstract class WebTestUtil extends TestUtil {

    private static final Log LOG = LogFactory.getLog(WebTestUtil.class);

    /**
     * the server root url. {@value}
     */
    public static final String BASE_TEST_URL = "http://localhost:9090/brmo-service/";


    /**
     * our test client.
     */
    protected static CloseableHttpClient client;


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

    /**
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

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
