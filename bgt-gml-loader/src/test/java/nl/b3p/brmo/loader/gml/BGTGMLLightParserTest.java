/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.util.NullProgressListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Geparametriseerde test om features uit BGT/PDOK gmllight formaat te parsen.
 * run: {@code mvn -Dtest=nl.b3p.brmo.loader.gml.BGTGMLLightParserTest test}.
 *
 * @author mprins
 */
//@RunWith(Parameterized.class)
public class BGTGMLLightParserTest {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightParserTest.class);
    private Parser parser;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                /*"gmlFileName", "typeNamespace", "elementName", expectedNumOfElements, expectedNumOfAtrr */
                arguments("/gmllight/bgt_ondersteunendwaterdeel.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OndersteunendWaterdeel", 47, 20),
                arguments("/gmllight/bgt_ongeclassificeerdobject.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OngeclassificeerdObject", 0, 18),
                arguments("/gmllight/bgt_buurt.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "Buurt", 10, 21),
                arguments("/gmllight/bgt_wijk.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "Wijk", 4, 21),
                arguments("/gmllight/bgt_openbareruimte.gml", "http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", "OpenbareRuimte", 26, 20)

        );
    }

    @BeforeEach
    public void setUpParser() {
        final String schemaLocation = BGTGMLLightParserTest.class.getResource("/imgeo-simple_resolved.xsd").getFile();

        Configuration configuration = new ApplicationSchemaConfiguration("http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", schemaLocation);
        configuration.getContext().registerComponentInstance(new GeometryFactory(new PrecisionModel(), 28992));

        parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setStrict(true);
        parser.setFailOnValidationError(false);
    }

    /**
     *
     * @param gmlFileName filename of the test resource (GML)
     * @param typeNamespace element type namespace
     * @param elementName element
     * @param expectedNumOfElements number of elements in resources
     * @param expectedNumOfAtrr verwacht aantal attributen
     *
     * @throws Exception if any
     */
    @DisplayName("Test GML file")
    @ParameterizedTest(name = "{index}: gml file: ''{0}''")
    @MethodSource("argumentsProvider")
    public void testGMLFile(String gmlFileName, String typeNamespace, String elementName, int expectedNumOfElements, int expectedNumOfAtrr)
            throws Exception {
        LOG.debug("Start testing for: " + typeNamespace + ":" + elementName);

        InputStream is = BGTGMLLightParserTest.class.getResourceAsStream(gmlFileName);

        FeatureCollection<?, ?> featureCollection = (FeatureCollection<?, ?>) parser.parse(is);
        assertEquals(expectedNumOfElements, featureCollection.size(),
                "Collection size should be as expected.");

        @SuppressWarnings("unchecked")
        List<Exception> errors = parser.getValidationErrors();
        assertTrue(errors.isEmpty(), "There should be no parsing errors");

        final Name elemTypeName = new NameImpl(typeNamespace, elementName);
        featureCollection.accepts(new AbstractFeatureVisitor() {
            @Override
            public void visit(Feature feature) {
                assertEquals(elemTypeName, feature.getType().getName(), "TypeName should match");

                Collection<Property> props = feature.getProperties();
                assertEquals(expectedNumOfAtrr, props.size(), "There should be a number of properties");
                String s = "Feature " + feature.getIdentifier() + "\n";
                for (Property p : props) {
                    s = s.concat(" - " + p.getName() + " (" + p.getType() + "):\t " + p.getValue() + "\n");
                }
                LOG.debug(s);
            }

        }, new NullProgressListener());
    }

}
