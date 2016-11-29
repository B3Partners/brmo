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
package nl.b3p.topnl;

import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBException;
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
public class ProcessorTest {
    
    private Processor instance;
    
    public ProcessorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws JAXBException {
        instance = new Processor();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of Processor method, of class Processor.
     */
    @Test
    public void testProcessor() {
        System.out.println("Processor");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class Processor.
     */
    @Test
    public void testParse() throws JAXBException {
        System.out.println("parse");
        InputStream in = ProcessorTest.class.getResourceAsStream("Hoogte.xml");
        Object jaxb = instance.parse(in);
        assertNotNull(jaxb);
        assertTrue (jaxb instanceof nl.b3p.topnl.top250nl.FeatureCollectionT250NLType);
    }

    /**
     * Test of convert method, of class Processor.
     */
    @Test
    public void testConvert250() throws JAXBException {
    }

    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        instance.save(null, null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
