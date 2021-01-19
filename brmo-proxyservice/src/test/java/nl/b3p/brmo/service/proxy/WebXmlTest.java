package nl.b3p.brmo.service.proxy;

import org.junit.jupiter.api.Test;

import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static nl.b3p.brmo.test.util.WebXmlTest.testWebXmlIsValidSchemaJavaEE6;

public class WebXmlTest {
    @Test
    public void validateWebXml() throws Exception {
        File webxml = new File("src/main/webapp/WEB-INF/web.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE6(webxml), "`web.xml` is niet geldig");
    }

    @Test
    public void validateTestWebXml() throws Exception {
        File webxml = new File("src/test/resources/web.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE6(webxml), "Test `web.xml` is niet geldig");
    }
}
