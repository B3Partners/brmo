/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.topnl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class TopNLTypeFactoryTest {
    private TopNLTypeFactory instance = new TopNLTypeFactory();
    
    public TopNLTypeFactoryTest() {
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
     * Test of getTopNLType method, of class TopNLTypeFactory.
     */
    //@Test
    public void testGetTopNLType50() throws JDOMException, IOException {
        System.out.println("getTopNLType");
        URL is = null;
        TopNLTypeFactory instance = new TopNLTypeFactory();
        TopNLType expResult = TopNLType.TOP50NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    @Test
    public void testGetTopNLType250() throws JDOMException, IOException {
        URL is = TopNLTypeFactoryTest.class.getResource("top250nl_Hoogte.xml");
        
        System.out.println("getTopNLType");
        TopNLType expResult = TopNLType.TOP250NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
    }
    
    
    @Test
    public void testGetTopNLType100() throws JDOMException, IOException {
        URL is = TopNLTypeFactoryTest.class.getResource("top100nl_Hoogte.xml");
        
        System.out.println("getTopNLType");
        TopNLType expResult = TopNLType.TOP100NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
    }
    
}
