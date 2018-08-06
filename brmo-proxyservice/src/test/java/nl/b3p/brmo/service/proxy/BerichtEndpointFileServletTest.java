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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Hashtable;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

/**
 *
 * @author mprins
 */
public class BerichtEndpointFileServletTest {

    private String saveDir = "/bericht-post-test-";
    private ServletUnitClient client;
    private ServletRunner sr;

    @Before
    public void setUp() throws Exception {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setLoggingHttpHeaders(true);
        HttpUnitOptions.setPostIncludesCharset(true);

        saveDir = System.getProperty("java.io.tmpdir", "/tmp") + saveDir + new BigInteger(130, new SecureRandom()).toString(32);
        sr = new ServletRunner();
        Hashtable<String, String> servletParams = new Hashtable();
        servletParams.put("save_dir", saveDir);
        sr.registerServlet("/post/brk", BerichtEndpointFileServlet.class.getName(), servletParams);
        client = sr.newClient();
    }

    @After
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
        assertNotNull("No response received", response);
        assertEquals("Response not OK", 200, response.getResponseCode());

        // servlet scrijft naar saveDir, /tmp/brk/post-op_<yyyy-MM-dd_HH-mm-ss-SSS>_<randomuniek>.xml
        File expected = new File(BerichtEndpointFileServletTest.class.getResource("/web.xml").getFile());
        File actual = Files.newDirectoryStream(new File(saveDir).toPath(), "*.{xml}").iterator().next().toFile();

        assertTrue("Bestand begint niet met 'post-op", actual.getName().startsWith("post-op"));
        assertTrue("File inhoud niet gelijk", FileUtils.contentEquals(
                expected,
                actual
        ));
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
     *
     * @throws Exception if any
     */
    @Test
    @Ignore("dit werkt niet zo, nog eens uitzoeken hoe dit moet met httpunit... (Not in GZIP format)")
    public void testPostGzippedBRK() throws Exception {
        WebRequest p = new PostMethodWebRequest(
                "http://localhost:8080/post/brk",
                BerichtEndpointFileServletTest.class.getResourceAsStream("/web.xml.gz"),
                "text/xml; charset=utf-8"
        );
        p.setHeaderField("Content-Encoding", "gzip");
        client.getClientProperties().setAcceptGzip(true);
        WebResponse response = client.getResponse(p);

        assertNotNull("No response received", response);
        assertEquals("Response not OK", 200, response.getResponseCode());

        // servlet schrijft naar saveDir, /tmp/brk/post-op_<yyyy-MM-dd_HH-mm-ss-SSS>_<randomuniek>.xml
        File expected = new File(BerichtEndpointFileServletTest.class.getResource("/web.xml").getFile());
        File actual = Files.newDirectoryStream(new File(saveDir).toPath(), "*.{xml}").iterator().next().toFile();

        assertTrue("Bestand begint niet met 'post-op", actual.getName().startsWith("post-op"));
        assertTrue("File inhoud niet gelijk", FileUtils.contentEquals(
                expected,
                actual
        ));
    }
}
