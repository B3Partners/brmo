/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.proxy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Hashtable;
import org.apache.commons.io.FileUtils;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author mprins
 */
class BerichtEndpointFileServletTest {

  private String saveDir = "/bericht-post-test-";
  private ServletUnitClient client;
  private ServletRunner sr;

  @BeforeEach
  @SuppressModernizer
  void setUp() {
    HttpUnitOptions.setDefaultCharacterSet("UTF-8");
    HttpUnitOptions.setLoggingHttpHeaders(true);
    HttpUnitOptions.setPostIncludesCharset(true);

    saveDir =
        System.getProperty("java.io.tmpdir", "/tmp")
            + saveDir
            + new BigInteger(130, new SecureRandom()).toString(32);
    sr = new ServletRunner();
    // modernizer zeurt over gebruik Hashtable ipv HashMap
    Hashtable<String, String> servletParams = new Hashtable<>();
    servletParams.put("save_dir", saveDir);
    sr.registerServlet("/post/brk2", BerichtEndpointFileServlet.class.getName(), servletParams);
    client = sr.newClient();
  }

  @AfterEach
  void tearDown() throws Exception {
    sr.shutDown();
    FileUtils.deleteQuietly(new File(saveDir));
  }

  @Test
  void testPostBRK() throws Exception {
    WebRequest p =
        new PostMethodWebRequest(
            "http://localhost:8080/post/brk2",
            BerichtEndpointFileServletTest.class.getResourceAsStream("/test.xml"),
            "text/xml; charset=utf-8'");
    WebResponse response = client.sendRequest(p);
    assertAll(
        () -> assertNotNull(response, "No response received"),
        () -> assertEquals(200, response.getResponseCode(), "Response not OK"),
        // side effect: servlet schrijft naar saveDir
        () ->
            assertTrue(Files.exists(new File(saveDir).toPath()), "save directory does not exist"));

    File expected =
        new File(BerichtEndpointFileServletTest.class.getResource("/test.xml").getFile());
    assertNotNull(expected, "Expected file not found");
    // servlet schrijft naar saveDir,
    // /tmp/brk/post-op_<yyyy-MM-dd_HH-mm-ss-SSS>_<randomuniek>.xml
    File actual =
        Files.newDirectoryStream(new File(saveDir).toPath(), "*.{xml}").iterator().next().toFile();

    assertTrue(actual.getName().startsWith("post-op"), "Bestand begint niet met 'post-op");
    assertTrue(FileUtils.contentEquals(expected, actual), "File inhoud niet gelijk");
  }
}
