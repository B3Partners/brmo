package nl.b3p.brmo.stufbg204;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * Integratie test utility klasse om online integratie tests te bouwen welke
 * tegen een brmo-stufbg204 kunnen worden uitgevoerd.
 *
 * @author Mark Prins
 */
public abstract class WebTestStub extends TestStub {

    public static final int HTTP_PORT = 9091;
    /**
     * the server root url. {@value}
     */
    public static final String BASE_TEST_URL = "http://localhost:" + HTTP_PORT + "/brmo-stufbg204/";

    /**
     * onze test client.
     */
    protected static CloseableHttpClient client;
    protected static HttpHost target;
    protected static HttpClientContext localContext;

    /**
     * initialize http client.
     */
    @BeforeAll
    public static void setUpClass() {
        target = new HttpHost("localhost", HTTP_PORT, "http");
        AuthCache authCache = new BasicAuthCache();
        authCache.put(target, new BasicScheme());

        localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials("brmo", "brmo"));

        client = HttpClients.custom()
                .useSystemProperties()
                .setUserAgent("brmo integration test")
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultCredentialsProvider(credsProvider)
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
