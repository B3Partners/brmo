/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BGTGMLLightValidatieTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightValidatieTest.class);

    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            // arrays van: {"gmlFileName", hasKruinlijnElements },
            {"/gmllight/kruinlijntest/bgt_ondersteunendwegdeel.gml", true}
        });
    }

    private final String gmlFileName;
    private final boolean hasKruinlijnElements;

    /**
     *
     * @param gmlFileName filename of the test resource (GML)
     * @param hasKruinlijnElements {@code true} if the file has kruinlijn
     * elements
     *
     * @see #params()
     */
    public BGTGMLLightValidatieTest(String gmlFileName, boolean hasKruinlijnElements) {
        this.gmlFileName = gmlFileName;
        this.hasKruinlijnElements = hasKruinlijnElements;
    }

    /**
     * probeer gml te valideren met ons schema.
     *
     * @throws Exception if any
     */
    @Test
    @Ignore("Als er kruinlijnen elementen zijn die als LineString in de GML zitten mislukt deze test")
    public void validateFile() throws Exception {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(new File(BGTLightKruinlijnIntegrationTest.class.getResource(gmlFileName).toURI()));

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(
                new File(BGTLightKruinlijnIntegrationTest.class.getResource("/imgeo-simple_resolved.xsd").toURI())
        );
        Schema schema = factory.newSchema(schemaFile);
        Validator validator = schema.newValidator();

        // validate DOM tree
        try {
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            // instance document is invalid!
            LOG.error("Valitatiefout: ", e);
            fail("Validation failed: " + e.getMessage());
        }
    }

}
