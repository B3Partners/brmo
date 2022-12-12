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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Meine Toonen
 */
public class DataComfortXMLReaderTest {

    private static final Log log = LogFactory.getLog(DataComfortXMLReaderTest.class);
    private static final DataComfortXMLReader reader = new DataComfortXMLReader();
    
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
    @Test
    public void testComfortdata() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("comfortdata.xml");
        StreamSource source = new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try {
            assertEquals(3, data.size(), "Er zijn drie table data elementen");
            TableData d = data.get(0);
            assertTrue(d.isComfortData(), "eerste table data is comfort data.");
            assertEquals(4, d.getRows().size(), "Er zijn vier table rows");
            TableRow row = d.getRows().get(0);
            assertEquals("50656082", row.getColumnValue("kvk_nummer"));

            d = data.get(2);
            row = d.getRows().get(0);
            assertEquals("214606.115 581137.695 214593.637 581184.181 214586.404 581200.432 214582.757 581198.853 214579.699 581197.328 214595.919 581135.491 214597.599 581135.854 214606.115 581137.695",
                    row.getColumnValue("posList"));


        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }
}
