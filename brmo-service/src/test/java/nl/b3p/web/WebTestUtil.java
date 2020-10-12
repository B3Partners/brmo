package nl.b3p.web;

import nl.b3p.brmo.service.testutil.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

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
    public static final String BASE_TEST_URL = "http://localhost:9091/brmo-service/";


    /**
     * our test client.
     */
    protected static CloseableHttpClient client;


    /**
     * initialize http client.
     */
    @BeforeAll
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
    @AfterAll
    public static void tearDownClass() throws IOException {
        client.close();
    }

}
