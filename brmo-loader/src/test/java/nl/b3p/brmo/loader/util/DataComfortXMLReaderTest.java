/*
 * Copyright (C) 2012-2015 B3Partners B.V.
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
package nl.b3p.brmo.loader.util;

import java.io.InputStream;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Meine Toonen
 */
public class DataComfortXMLReaderTest {

    private static final Log log = LogFactory.getLog(DataComfortXMLReaderTest.class);
    private static DataComfortXMLReader reader = new DataComfortXMLReader();
    
    @Before
    public void setUp(){
        log.debug("Testing DataComfortXMLReader");
    }

    @After
    public void tearDown() {
    }

    
    @Test
    public void testEmptyStackExceptionXML() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("EmptyStackException.xml");
        StreamSource source= new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try{
            assertEquals(6, data.size());
        }catch (Exception e){
            fail(e.getLocalizedMessage());
        }
    }
    @Test
    public void testClassCastExceptionXML() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("classCastException.xml");
        StreamSource source= new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try{
            assertEquals(6, data.size());
        }catch (Exception e){
            fail(e.getLocalizedMessage());
        }
    }

}
