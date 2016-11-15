/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.xml;

import org.geotools.xml.TOP250NLParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.geotools.factory.GeoTools;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author Meine Toonen
 */
public class TOP250NLParserTest {

    private TOP250NLParser parser;

    static {
        GeoTools.init();
    }
    @Before
    public void init() throws JAXBException{
        parser = new TOP250NLParser();
    }
    
    
   // @Test
    public void testParseHoogte() throws IOException, SAXException, ParserConfigurationException{
        InputStream in = TOP250NLParser.class.getResourceAsStream("Hoogte.xml");
          Object col = parser.parseFeatures(in);
        //assertEquals(1,col.size());
        assertNotNull(col);
        assertEquals(SimpleFeature.class, col.getClass());
    }
    
    @Test
    public void testParseFunctioneelGebied() throws IOException, SAXException, ParserConfigurationException{
        InputStream in = TOP250NLParser.class.getResourceAsStream("FunctioneelGebied.xml");
        Object col = parser.parseFeatures(in);
        //assertEquals(1,col.size());
        assertNotNull(col);
        assertEquals(SimpleFeature.class, col.getClass());
    }
    
   // @Test
    public void testParse2FunctioneelGebied() throws IOException, SAXException, ParserConfigurationException{
        InputStream in = TOP250NLParser.class.getResourceAsStream("2FunctioneelGebied.xml");
        Object col = parser.parseFeatures(in);
        //assertEquals(1,col.size());
        assertNotNull(col);
        assertEquals(SimpleFeatureCollection.class, col.getClass());
    }
    
}
