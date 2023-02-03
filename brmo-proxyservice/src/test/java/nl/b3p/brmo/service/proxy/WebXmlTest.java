package nl.b3p.brmo.service.proxy;

import static nl.b3p.brmo.test.util.WebXmlTest.testWebXmlIsValidSchemaJavaEE7;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

class WebXmlTest {
  @Test
  void validateWebXml() throws Exception {
    File webxml = new File("src/main/webapp/WEB-INF/web.xml");
    assertTrue(testWebXmlIsValidSchemaJavaEE7(webxml), "`web.xml` is niet geldig");
  }

  @Test
  void validateTestWebXml() throws Exception {
    File webxml = new File("src/test/resources/web.xml");
    assertTrue(testWebXmlIsValidSchemaJavaEE7(webxml), "Test `web.xml` is niet geldig");
  }
}
