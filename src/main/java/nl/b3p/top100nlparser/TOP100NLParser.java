/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.top100nlparser;

import nl.b3p.top250nlparser.*;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class TOP100NLParser {

    private Parser parser; 
    private JAXBContext context = null;


    public TOP100NLParser() throws JAXBException {
        String schemaLocation = TOP100NLParser.class.getResource("standaardGML/XSD-Resolver/finalized/top100nl_resolved.xsd").toString();
        Configuration configuration = new ApplicationSchemaConfiguration("http://register.geostandaarden.nl/gmlapplicatieschema/top100nl/1.1.0", schemaLocation);
        configuration.getContext().registerComponentInstance(new GeometryFactory(new PrecisionModel(), 28992));

        parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setStrict(false);
        parser.setFailOnValidationError(false);
        
        
        context = JAXBContext.newInstance("piet");
    }

    public List<String> getTypeNames() throws IOException {
        // URL schemaLocation = TOPParser.class.getResource( "top250nl.xsd");
        URL schemaLocation = TOP100NLParser.class.getResource("XSD-Resolver/finalized/top100nl_resolved.xsd");

        GML gml = new GML(Version.WFS1_1);
        gml.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);

        SimpleFeatureType featureType = gml.decodeSimpleFeatureType(schemaLocation, new NameImpl(
                "http://register.geostandaarden.nl/gmlapplicatieschema/top100nl/1.1.0", "FunctioneelGebied"));

        List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();
        List<String> names = new ArrayList<String>(attributes.size());
        for (AttributeDescriptor desc : attributes) {
            names.add(desc.getLocalName());
        }
        return names;
    }

    public SimpleFeatureCollection parseFeatures(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        // URL schemaLocation = TOPParser.class.getResource( "top250nl.xsd");

        Object obj = parser.parse(in);
        SimpleFeatureCollection col = (SimpleFeatureCollection) obj;
        return col;
    }
    
    public Object jaxbIt(URL u) throws JAXBException{
         Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
        JAXBElement o = (JAXBElement) jaxbUnmarshaller.unmarshal(u);

        Object value = o.getValue();
        
        int a = 0;
        return a;

    }

}
