/*
 * Copyright (C) 2016 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.top100nlparser;

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
     //   gml.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);

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
