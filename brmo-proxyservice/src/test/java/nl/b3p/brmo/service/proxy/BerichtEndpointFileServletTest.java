/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.proxy;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mprins
 */
public class BerichtEndpointFileServletTest {

    private String saveDir = "/bericht-post-test-";
    private ServletUnitClient client;
    private ServletRunner sr;

    @BeforeEach
    public void setUp() throws Exception {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setLoggingHttpHeaders(true);
        HttpUnitOptions.setPostIncludesCharset(true);

        saveDir = System.getProperty("java.io.tmpdir", "/tmp") + saveDir + new BigInteger(130, new SecureRandom()).toString(32);
        sr = new ServletRunner();
        Hashtable<String, String> servletParams = new Hashtable<>();
        servletParams.put("save_dir", saveDir);
        sr.registerServlet("/post/brk", BerichtEndpointFileServlet.class.getName(), servletParams);
        client = sr.newClient();
    }

    @AfterEach
    public void tearDown() throws Exception {
        sr.shutDown();
        FileUtils.cleanDirectory(new File(saveDir));
        FileUtils.deleteQuietly(new File(saveDir));
    }

    @Test
    public void testPostBRK() throws Exception {
        WebRequest p = new PostMethodWebRequest(
                "http://localhost:8080/post/brk",
                BerichtEndpointFileServletTest.class.getResourceAsStream("/web.xml"),
                "text/xml; charset=utf-8'"
        );
        WebResponse response = client.getResponse(p);
        assertNotNull(response, "No response received");
        assertEquals(200, response.getResponseCode(), "Response not OK");

        // servlet scrijft naar saveDir, /tmp/brk/post-op_<yyyy-MM-dd_HH-mm-ss-SSS>_<randomuniek>.xml
        File expected = new File(BerichtEndpointFileServletTest.class.getResource("/web.xml").getFile());
        File actual = Files.newDirectoryStream(new File(saveDir).toPath(), "*.{xml}").iterator().next().toFile();

        assertTrue(actual.getName().startsWith("post-op"), "Bestand begint niet met 'post-op");
        assertTrue(FileUtils.contentEquals(
                expected,
                actual
        ), "File inhoud niet gelijk");
    }

    /**
     * met de hand testen tegen draaiende servlet (met en zonder crompressie):
     * <ul>
     * <li>{@code
     * curl -v -s --trace-ascii http_trace.log --data-binary @'brmo-loader/src/test/resources/verminderenstukdelen/MUTKX01-ASN00V2937-Bericht1.xml.gz' -H "Content-Type: text/xml" -H "Content-Encoding: gzip" -X POST http://localhost:8037/brmo-proxyservice/post/brk
     * }</li>
     * <li>{@code curl -v -s --trace-ascii http_trace.log -d @'brmo-loader/src/test/resources/verminderenstukdelen/MUTKX01-ASN00V2937-Bericht1.xml' -H "Content-Type: application/xml" -X POST http://localhost:8037/brmo-proxyservice/post/brk
     * }</li>
     * </ul>
     * @todo dit werkt niet zo, nog eens uitzoeken hoe dit moet met httpunit... (Not in GZIP format melding)
     * @throws Exception if any
     */
    @Test
    @Disabled("dit werkt niet zo, nog eens uitzoeken hoe dit moet met httpunit... (Not in GZIP format melding)")
    public void testPostGzippedBRK() throws Exception {
        WebRequest p = new PostMethodWebRequest(
                "http://localhost:8080/post/brk",
                BerichtEndpointFileServletTest.class.getResourceAsStream("/web.xml.gz"),
                "text/xml; charset=utf-8"
        );
        p.setHeaderField("Content-Encoding", "gzip");
        client.getClientProperties().setAcceptGzip(true);
        WebResponse response = client.getResponse(p);

        assertNotNull(response, "No response received");
        assertEquals(200, response.getResponseCode(), "Response not OK");

        // servlet schrijft naar saveDir, /tmp/brk/post-op_<yyyy-MM-dd_HH-mm-ss-SSS>_<randomuniek>.xml
        File expected = new File(BerichtEndpointFileServletTest.class.getResource("/web.xml").getFile());
        File actual = Files.newDirectoryStream(new File(saveDir).toPath(), "*.{xml}").iterator().next().toFile();

        assertTrue(actual.getName().startsWith("post-op"), "Bestand begint niet met 'post-op");
        assertTrue(FileUtils.contentEquals(
                expected,
                actual
        ), "File inhoud niet gelijk");
    }
}
