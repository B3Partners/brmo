package nl.b3p.bgtparser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
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
public class BGTParserTest {
 
    private BGTParser parser;
    @Before
    public void init() throws JAXBException {
        parser = new BGTParser();
    }

    @Test
    public void testParse() throws IOException, SAXException, ParserConfigurationException{
        InputStream in = BGTParserTest.class.getResourceAsStream("bgt_gebouwinstallatie.gml");
        SimpleFeatureCollection col = parser.parseFeatures(in);
        assertEquals(23,col.size());
    }
    
}
