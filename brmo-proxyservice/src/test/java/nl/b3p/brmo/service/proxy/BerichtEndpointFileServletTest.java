/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.proxy;

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
        saveDir = System.getProperty("java.io.tmpdir", "/tmp") + saveDir + new BigInteger(130, new SecureRandom()).toString(32);
        // sr = new ServletRunner(BerichtEndpointFileServletTest.class.getResourceAsStream("/web.xml"));
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
        WebRequest p = new PostMethodWebRequest("http://localhost:8080/post/brk", BerichtEndpointFileServletTest.class.getResourceAsStream("/web.xml"), "text/xml");
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
}
