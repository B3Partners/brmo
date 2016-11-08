/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.top250nlparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 *
 * @author Meine Toonen
 */
public class TOP250NLParserTest {
 
    private TOP250NLParser parser;
    @Before
    public void init() throws JAXBException{
        parser = new TOP250NLParser();
    }
    
   // @Test
    public void testFeaturetypes() throws IOException{
        List<String> names = parser.getTypeNames();
        assertEquals(4, names.size());
    }
    
   // @Test
    public void testParse() throws IOException, SAXException, ParserConfigurationException{
        InputStream in = TOP250NLParser.class.getResourceAsStream("Hoogte.xml");
        SimpleFeatureCollection col = parser.parseFeatures(in);
        assertEquals(1,col.size());
    }
    
    @Test
    public void testJaxbit() throws JAXBException{
        URL u = TOP250NLParser.class.getResource("Hoogte.xml");
        Object o = parser.jaxbIt(u);
        assertNotNull(o);
        
    }
}
