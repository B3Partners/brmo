package nl.b3p.brmo.service;

import org.junit.jupiter.api.Test;

import java.io.File;

import static nl.b3p.brmo.test.util.WebXmlTest.testWebXmlIsValidSchemaJavaEE6;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebXmlTest {
    @Test
    public void validateWebXml() throws Exception {
        File webxml = new File("src/main/webapp/WEB-INF/web.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE6(webxml), "`web.xml` is niet geldig");
    }
}
