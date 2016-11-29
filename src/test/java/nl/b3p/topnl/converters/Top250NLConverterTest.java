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
package nl.b3p.topnl.converters;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import nl.b3p.topnl.Processor;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author meine
 */
public class Top250NLConverterTest {
    
    private final Top250NLConverter instance;
    private final Processor processor;
    
    public Top250NLConverterTest() throws JAXBException {
        this.processor = new Processor();
        this.instance = new Top250NLConverter();
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of convert method, of class Top250NLConverter.
     */
    @Test
    public void testConvertFeatureCollection() throws JAXBException {
        System.out.println("convert");
        Hoogte hoogte = new Hoogte();
        InputStream in = Top250NLConverterTest.class.getResourceAsStream("FeatureCollectionHoogte.xml");
        Object jaxb = processor.parse(in);
        
        List<TopNLEntity> expResult = Collections.singletonList(hoogte);
        List<TopNLEntity> result = instance.convert(jaxb);
        assertNotNull(result);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).getClass(), result.get(0).getClass());
    }
    
    @Test
    public void testConvertNoFeatureCollection() throws JAXBException {
        System.out.println("convert");
        Hoogte hoogte = new Hoogte();
        InputStream in = Top250NLConverterTest.class.getResourceAsStream("Hoogte.xml");
        Object jaxb = processor.parse(in);
        
        List<TopNLEntity> expResult = Collections.singletonList(hoogte);
        List<TopNLEntity> result = instance.convert(jaxb);
        assertNotNull(result);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).getClass(), result.get(0).getClass());
    }

    /**
     * Test of convertHoogte method, of class Top250NLConverter.
     */
    @Test
    public void testConvertHoogte() throws JAXBException {
        System.out.println("convert");
        InputStream in = Top250NLConverterTest.class.getResourceAsStream("Hoogte.xml");
        Object jaxb = processor.parse(in);
        TopNLEntity entity = instance.convertObject(jaxb);
        
        assertNotNull(entity);
        assertTrue(entity instanceof Hoogte);
    }
    
}
