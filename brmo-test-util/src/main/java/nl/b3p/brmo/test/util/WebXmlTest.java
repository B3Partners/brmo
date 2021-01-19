package nl.b3p.brmo.test.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlunit.validation.JAXPValidator;
import org.xmlunit.validation.ValidationProblem;
import org.xmlunit.validation.ValidationResult;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

public final class WebXmlTest {

    private static final Log LOG = LogFactory.getLog(WebXmlTest.class);
    private static final String WEB_XML_SCHEMA = "/webxml-schema/";

    private WebXmlTest() {
    }

    /**
     * test of {@code web.xml} geldig is als Java EE 6 (Tomcat 7) config.
     *
     * @param webxml de te valideren {@code web.xml}
     * @return {@code true} als geldig, anders {@code false}; logging toont problemen
     * @throws URISyntaxException when one of the embedded schemafile URLs cannot be converted to a URI
     */
    public static boolean testWebXmlIsValidSchemaJavaEE6(File webxml) throws URISyntaxException {
        JAXPValidator validator = new JAXPValidator(W3C_XML_SCHEMA_NS_URI);
        validator.setSchemaSources(
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE6/web-app_3_0.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE6/web-app_3_0.xsd").toURI().toString()),
                new StreamSource(
                        WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE6/web-common_3_0.xsd"),
                        WebXmlTest.class.getResource(
                                WEB_XML_SCHEMA + "JavaEE6/web-common_3_0.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE6/javaee_6.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE6/javaee_6.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(
                        WEB_XML_SCHEMA + "JavaEE6/javaee_web_services_client_1_3.xsd"),
                        WebXmlTest.class.getResource(
                                WEB_XML_SCHEMA + "JavaEE6/javaee_web_services_client_1_3.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE6/jsp_2_2.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE6/jsp_2_2.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "xml.xsd"))
        );
        return validate(validator, webxml);
    }

    /**
     * test of {@code web.xml} geldig is als Java EE 7 (Tomcat 8) config.
     *
     * @param webxml de te valideren {@code web.xml}
     * @return {@code true} als geldig, anders {@code false}; logging toont problemen
     * @throws URISyntaxException when one of the embedded schemafile URLs cannot be converted to a URI
     */
    public static boolean testWebXmlIsValidSchemaJavaEE7(File webxml) throws URISyntaxException {
        JAXPValidator validator = new JAXPValidator(W3C_XML_SCHEMA_NS_URI);
        validator.setSchemaSources(
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE7/web-app_3_1.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE7/web-app_3_1.xsd").toURI().toString()),
                new StreamSource(
                        WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE7/web-common_3_1.xsd"),
                        WebXmlTest.class.getResource(
                                WEB_XML_SCHEMA + "JavaEE7/web-common_3_1.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE7/javaee_7.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE7/javaee_7.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(
                        WEB_XML_SCHEMA + "JavaEE7/javaee_web_services_client_1_4.xsd"),
                        WebXmlTest.class.getResource(
                                WEB_XML_SCHEMA + "JavaEE7/javaee_web_services_client_1_4.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE7/jsp_2_3.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE7/jsp_2_3.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "xml.xsd"))
        );
        return validate(validator, webxml);
    }

    /**
     * test of {@code web.xml} geldig is als Java EE 8 (Tomcat 9) config.
     *
     * @param webxml de te valideren {@code web.xml}
     * @return {@code true} als geldig, anders {@code false}; logging toont problemen
     * @throws URISyntaxException when one of the embedded schemafile URLs cannot be converted to a URI
     */
    public static boolean testWebXmlIsValidSchemaJavaEE8(File webxml) throws URISyntaxException {
        JAXPValidator validator = new JAXPValidator(W3C_XML_SCHEMA_NS_URI);
        validator.setSchemaSources(
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE8/web-app_4_0.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE8/web-app_4_0.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE8/web-common_4_0.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE8/web-common_4_0.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE8/javaee_8.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE8/javaee_8.xsd").toURI().toString()),
                // worden gedeeld met Java EE 7
                new StreamSource(WebXmlTest.class.getResourceAsStream(
                        WEB_XML_SCHEMA + "JavaEE7/javaee_web_services_client_1_4.xsd"),
                        WebXmlTest.class.getResource(
                                WEB_XML_SCHEMA + "JavaEE7/javaee_web_services_client_1_4.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "JavaEE7/jsp_2_3.xsd"),
                        WebXmlTest.class.getResource(WEB_XML_SCHEMA + "JavaEE7/jsp_2_3.xsd").toURI().toString()),
                new StreamSource(WebXmlTest.class.getResourceAsStream(WEB_XML_SCHEMA + "xml.xsd"))
        );

        return validate(validator, webxml);
    }

    private static boolean validate(JAXPValidator validator, File webxml) {
        ValidationResult result = validator.validateInstance(new StreamSource(webxml));
        if (!result.isValid()) {
            Iterator<ValidationProblem> problems = result.getProblems().iterator();
            while (problems.hasNext()) {
                ValidationProblem problem = problems.next();
                LOG.error(problem.toString());
            }
        }
        return result.isValid();
    }
}
