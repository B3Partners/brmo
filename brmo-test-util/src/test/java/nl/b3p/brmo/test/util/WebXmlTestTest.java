package nl.b3p.brmo.test.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static nl.b3p.brmo.test.util.WebXmlTest.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class WebXmlTestTest {

    @Test
    public void validWebXml() throws Exception {
        File webxml = new File("src/test/resources/web-valid-jee6.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE6(webxml), "`web.xml` is niet geldig");
    }

    @Test
    public void validWebXmlJEE7() throws Exception {
        File webxml = new File("src/test/resources/web-valid-jee7.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE7(webxml), "`web.xml` is niet geldig");
    }

    @Test
    public void validWebXmlJEE8() throws Exception {
        File webxml = new File("src/test/resources/web-valid-jee8.xml");
        assertTrue(testWebXmlIsValidSchemaJavaEE8(webxml), "`web.xml` is niet geldig");
    }

    @Test
    public void invalidWebXml() throws Exception {
        File webxml = new File("src/test/resources/web-invalid-jee6.xml");
        assertFalse(testWebXmlIsValidSchemaJavaEE6(webxml), "`web.xml` is geldig");
    }
}
