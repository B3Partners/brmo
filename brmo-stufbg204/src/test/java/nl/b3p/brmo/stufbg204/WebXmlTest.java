package nl.b3p.brmo.stufbg204;

import static nl.b3p.brmo.test.util.WebXmlTest.testWebXmlIsValidSchemaJavaEE7;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.junit.jupiter.api.Test;

@SuppressModernizer
public class WebXmlTest {
  @Test
  public void validateWebXml() throws Exception {
    File webxml = new File("src/main/webapp/WEB-INF/web.xml");
    assertTrue(testWebXmlIsValidSchemaJavaEE7(webxml), "`web.xml` is niet geldig");
  }
}
