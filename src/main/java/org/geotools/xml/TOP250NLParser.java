/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class TOP250NLParser {

    private Parser parser; 
    private JAXBContext context = null;


    public TOP250NLParser() throws JAXBException {
        Hints.putSystemDefault(Hints.ENTITY_RESOLVER, NullEntityResolver.INSTANCE);
        Hints.scanSystemProperties();
        
        final String schemaLocation = TOP250NLParser.class.getResource("top250nl_resolved.xsd").toString();
        Configuration topNLConfig = new ApplicationSchemaConfiguration("http://register.geostandaarden.nl/gmlapplicatieschema/top250nl/1.2.0", schemaLocation);
       
       /* final String gmlSchemaLocation = TOP250NLParser.class.getResource("gml_resolved.xsd").toString();
        Configuration gmlConfig = new ApplicationSchemaConfiguration("http://www.opengis.net/gml/3.2", gmlSchemaLocation);
        topNLConfig.getDependencies().add(gmlConfig);*/
        /*
        final String brtSchemaLocation = TOP250NLParser.class.getResource("brt-algemeen_resolved.xsd").toString();
        Configuration brtConfig = new ApplicationSchemaConfiguration("http://register.geostandaarden.nl/gmlapplicatieschema/brt-algemeen/1.2.0", brtSchemaLocation);
        
        */
            
        parser = new Parser(topNLConfig);
        
        parser.setEntityResolver( GeoTools.getEntityResolver(GeoTools.getDefaultHints() ) );
       /* final String brtSchemaLocation = TOP250NLParser.class.getResource("brt-algemeen_resolved.xsd").toString();
        Configuration brtConfig = new ApplicationSchemaConfiguration("http://register.geostandaarden.nl/gmlapplicatieschema/brt-algemeen/1.2.0", brtSchemaLocation);
        
    
        final String schemaLocation = TOP250NLParser.class.getResource("top250nl_resolved.xsd").toString();
        Configuration configuration = new MeineConfiguration("http://register.geostandaarden.nl/gmlapplicatieschema/top250nl/1.2.0", schemaLocation, configs);
        */
      //  parser = new Parser(topNLConfig);
        parser.setValidating(false);
        parser.setStrict(false);
        parser.setFailOnValidationError(false);
        
        context = JAXBContext.newInstance("jaxb");
        URL u = XSD.class.getResource("gml_resolved.xml");
        int a = 0;
    }

    public Object parseFeatures(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        // URL schemaLocation = TOPParser.class.getResource( "top250nl.xsd");

        Object obj = parser.parse(in);
    //    SimpleFeature col = (SimpleFeature) obj;
        return obj;

    }
    
    public Object jaxbIt(URL u) throws JAXBException{
        return jaxbIt(u, context);
    }
    
    public Object jaxbIt(URL u, JAXBContext c) throws JAXBException{
         Unmarshaller jaxbUnmarshaller = c.createUnmarshaller();
        JAXBElement o = (JAXBElement) jaxbUnmarshaller.unmarshal(u);

        Object value = o.getValue();
        SimpleFeatureCollection col = (SimpleFeatureCollection) value;
        
        return col;
        
    }

}
