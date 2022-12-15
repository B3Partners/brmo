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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Meine Toonen
 */
public class DataComfortXMLReaderTest {

    private static final Log log = LogFactory.getLog(DataComfortXMLReaderTest.class);
    private static final DataComfortXMLReader reader = new DataComfortXMLReader();
    
    @BeforeEach
    public void setUp(){
        log.debug("Testing DataComfortXMLReader");
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testEmptyStackExceptionXML() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("EmptyStackException.xml");
        StreamSource source= new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try{
            assertEquals(22, data.size());
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
            assertEquals(15, data.size());
        }catch (Exception e){
            fail(e.getLocalizedMessage());
        }
    }
    @Test
    public void test() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("comfortdata.xml");
        StreamSource source = new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try {
            assertEquals(9, data.size(), "Er zijn drie table data elementen");
            TableData d = data.get(0);
            assertTrue(d.isComfortData(), "eerste table data is comfort data.");
            assertEquals(4, d.getRows().size(), "Er zijn vier table rows");
            TableRow row = d.getRows().get(0);
            assertEquals("50656082", row.getColumnValue("kvk_nummer"));

            d = data.get(2);
            row = d.getRows().get(0);
            assertEquals("MULTIPOLYGON (((214606.115 581137.695, 214593.637 581184.181, 214586.404 581200.432, 214582.757 581198.853, 214579.699 581197.328, 214595.919 581135.491, 214597.599 581135.854, 214606.115 581137.695)))",
                    row.getColumnValue("begrenzing_perceel"));

    @Test
    public void testBrk2() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("comfortdata-brk2.xml");
        StreamSource source = new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        assertEquals(7, data.size(), "Er zijn niet 7 table data elementen");
        TableData d = data.get(2);
        assertTrue(d.isComfortData(), "3e table data is comfort data.");
        assertEquals(2, d.getRows().size(), "Er zijn vier table rows");
        TableRow row = d.getRows().get(1);
        assertEquals("21013149", row.getColumnValue("kvknummer"));

        d = data.get(6);
        row = d.getRows().get(0);
        assertEquals("NL.IMKAD.KadastraalObject.50247970000", row.getColumnValue("identificatie"));
        assertEquals("19452.172 366623.187 19476.238 366616.882 19477.741 366622.664 19453.125 366629.117 19452.172 366623.187",
                row.getColumnValue("posList"));
    }

    @Test
    public void testGml32() throws Exception {
        InputStream stream = DataComfortXMLReaderTest.class.getResourceAsStream("comfortdata32.xml");
        StreamSource source = new StreamSource(stream);
        List<TableData> data = reader.readDataXML(source);
        try {
            assertEquals(9, data.size(), "Er zijn drie table data elementen");
            TableData d = data.get(6);
            assertEquals(1, d.getRows().size(), "Er is één table row");
            TableRow row = d.getRows().get(0);
            assertEquals("perceel", row.getTable());
            assertEquals("MULTIPOLYGON (((19452.172 366623.187, 19476.238 366616.882, 19477.741 366622.664, 19453.125 366629.117, 19452.172 366623.187)))", row.getValues().get(2));
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }
}
