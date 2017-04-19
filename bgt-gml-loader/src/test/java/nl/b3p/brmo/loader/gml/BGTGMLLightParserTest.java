/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.util.NullProgressListener;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.xml.sax.SAXException;

/**
 * Geparametriseerde test om features uit BGT/PDOK gmllight formaat te parsen.
 * run: {@code mvn -Dtest=nl.b3p.brmo.loader.gml.BGTGMLLightParserTest test}.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BGTGMLLightParserTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightParserTest.class);

    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            //arrays of: {"gmlFileName", "typeNamespace", "elementName", expectedNumOfElements, expectedNumOfAtrr},
            // NB. expectedNumOfAtrr bevat ook generieke GML attributen
            {"/gmllight/bgt_ondersteunendwaterdeel.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OndersteunendWaterdeel", 47, 20},
            {"/gmllight/bgt_ongeclassificeerdobject.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OngeclassificeerdObject", 0, 18},
            {"/gmllight/bgt_buurt.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "Buurt", 10, 21},
            {"/gmllight/bgt_wijk.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "Wijk", 4, 21},
            {"/gmllight/bgt_openbareruimte.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OpenbareRuimte", 26, 20}
        });
    }
    private Parser parser;

    private final String gmlFileName;
    private final String typeNamespace;
    private final String elementName;
    private final int expectedNumOfElements;
    private final int expectedNumOfAtrr;

    /**
     *
     * @param gmlFileName filename of the test resource (GML)
     * @param typeNamespace element type namespace
     * @param elementName element
     * @param expectedNumOfElements number of elements in resources
     * @param expectedNumOfAtrr verwacht aantal attributen
     *
     * @see #params()
     */
    public BGTGMLLightParserTest(String gmlFileName, String typeNamespace, String elementName, int expectedNumOfElements, int expectedNumOfAtrr) {
        this.gmlFileName = gmlFileName;
        this.typeNamespace = typeNamespace;
        this.elementName = elementName;
        this.expectedNumOfElements = expectedNumOfElements;
        this.expectedNumOfAtrr = expectedNumOfAtrr;
    }

    @Before
    public void setUpParser() {
        final String schemaLocation = BGTGMLLightParserTest.class.getResource("/imgeo-simple_resolved.xsd").getFile();

        Configuration configuration = new ApplicationSchemaConfiguration("http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", schemaLocation);
        configuration.getContext().registerComponentInstance(new GeometryFactory(new PrecisionModel(), 28992));

        parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setStrict(true);
        parser.setFailOnValidationError(false);
    }

    @Test
    public void testGMLFile() throws IOException, SAXException, ParserConfigurationException {
        LOG.debug("Start testing for: " + typeNamespace + ":" + elementName);

        InputStream is = BGTGMLLightParserTest.class.getResourceAsStream(gmlFileName);

        FeatureCollection<?, ?> featureCollection = (FeatureCollection<?, ?>) parser.parse(is);
        assertEquals("Collection size should be as expected.", expectedNumOfElements, featureCollection.size());

        @SuppressWarnings("unchecked")
        List<Exception> errors = parser.getValidationErrors();
        assertTrue("There should be no parsing errors", errors.isEmpty());

        final Name elemTypeName = new NameImpl(typeNamespace, elementName);
        featureCollection.accepts(new AbstractFeatureVisitor() {
            @Override
            public void visit(Feature feature) {
                assertEquals("TypeName should match", elemTypeName, feature.getType().getName());

                Collection<Property> props = feature.getProperties();
                assertEquals("There should be a number of properties", expectedNumOfAtrr, props.size());
                String s = "Feature " + feature.getIdentifier() + "\n";
                for (Property p : props) {
                    s = s.concat(" - " + p.getName() + " (" + p.getType() + "):\t " + p.getValue() + "\n");
                }
                LOG.debug(s);
            }

        }, new NullProgressListener());
    }

}
