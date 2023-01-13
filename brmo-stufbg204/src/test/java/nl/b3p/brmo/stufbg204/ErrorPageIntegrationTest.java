package nl.b3p.brmo.stufbg204;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ErrorPageIntegrationTest extends WebTestStub {

    /** onze test response. */
    private HttpResponse response;

    @Test
    public void test404() throws IOException {
        response = client.execute(new HttpGet(BASE_TEST_URL + "/styles/"));

        final String body = EntityUtils.toString(response.getEntity());
        assertEquals(
                HttpStatus.SC_NOT_FOUND,
                response.getStatusLine().getStatusCode(),
                "Response status is niet 404.");
        assertNotNull(body, "Response body mag niet null zijn.");
        // generieke error pagina bevat Tomcat + versienummer; dat willen we niet zien
        assertFalse(body.contains("Tomcat"));
    }

    @Override
    public void setUp() throws Exception {
        /* void implementatie */
    }
}
