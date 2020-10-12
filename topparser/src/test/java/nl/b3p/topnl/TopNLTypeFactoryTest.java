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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 * @author mprins
 *
 */
public class TopNLTypeFactoryTest {

    private final static Log LOG = LogFactory.getLog(TopNLTypeFactoryTest.class);
    private TopNLTypeFactory instance = new TopNLTypeFactory();


    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    /**
     * Test of getTopNLType method, of class TopNLTypeFactory.
     */
    @Test
    public void testGetTopNLType50() throws JDOMException, IOException {
        URL is = TopNLTypeFactoryTest.class.getResource("top50nl_Gebouw.xml");
        TopNLType expResult = TopNLType.TOP50NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetTopNLType250() throws JDOMException, IOException {
        URL is = TopNLTypeFactoryTest.class.getResource("top250nl_Hoogte.xml");

        TopNLType expResult = TopNLType.TOP250NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
    }
    
    
    @Test
    public void testGetTopNLType100() throws JDOMException, IOException {
        URL is = TopNLTypeFactoryTest.class.getResource("top100nl_Hoogte.xml");
        
        TopNLType expResult = TopNLType.TOP100NL;
        TopNLType result = instance.getTopNLType(is);
        assertEquals(expResult, result);
    }
    
}
