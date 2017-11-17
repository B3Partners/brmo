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

import com.vividsolutions.jts.io.ParseException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author meine
 */
public class ProcessorTest extends TestUtil{
    
    private Processor instance;
    
    public ProcessorTest() {
        this.useDB = true; 
    }

    @Before
    public void setUp() throws JAXBException, SQLException {
        instance = new Processor(datasource);
    }

    /**
     * Test of parse method, of class Processor.
     */
    @Test
    public void testParse() throws JAXBException, IOException {
        URL in = ProcessorTest.class.getResource("top250nl_Hoogte.xml");
        List jaxb = instance.parse(in);
        assertNotNull(jaxb);
        assertEquals(1, jaxb.size());
        assertTrue (jaxb.get(0) instanceof nl.b3p.topnl.top250nl.FeatureMemberType);
    }


    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testSave250() throws JAXBException, IOException, SAXException, TransformerException, ParserConfigurationException, SQLException, ParseException {
        URL in = ProcessorTest.class.getResource("top250nl_Hoogte.xml");
        TopNLType type = TopNLType.TOP250NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, type);
        instance.save(hoogte.get(0), type);

        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Hoogte real = run.query("SELECT * FROM top250nl.Hoogte WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000084246");
        assertNotNull(real);
    }
    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testImportIntoDb250() throws JAXBException, IOException, SAXException, TransformerException, ParserConfigurationException, SQLException, ParseException, JDOMException {
        URL in = ProcessorTest.class.getResource("top250nl_Hoogte.xml");
        TopNLType type = TopNLType.TOP250NL;
        instance.importIntoDb(in, type);
        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Hoogte real = run.query("SELECT * FROM top250nl.Hoogte WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000084246");
        assertNotNull(real);
    }

    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testSave250MultipleFeature() throws JAXBException, IOException, SAXException, TransformerException, ParserConfigurationException, SQLException, ParseException {
        URL in = ProcessorTest.class.getResource("top250nl_HoogteMulti.xml");
        TopNLType type = TopNLType.TOP250NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, type);
        instance.save(hoogte, type);

        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Hoogte real1 = run.query("SELECT * FROM top250nl.Hoogte WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000084246");
        assertNotNull(real1);
        Hoogte real2 = run.query("SELECT * FROM top250nl.Hoogte WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000084247");
        assertNotNull(real2);
    }
    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testSave100() throws JAXBException, IOException, SAXException, TransformerException, ParserConfigurationException, SQLException, ParseException {
        URL in = ProcessorTest.class.getResource("top100nl_Hoogte.xml");
        TopNLType type = TopNLType.TOP100NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, type);
        instance.save(hoogte.get(0), type);

        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Hoogte real = run.query("SELECT * FROM top100nl.Hoogte WHERE identificatie=?", handler, "NL.TOP100NL.16R11-0001468165");
        assertNotNull(real);
    }

    /**
     * Test of convert method, of class Processor.
     */
    @Test
    public void testConvert() throws Exception {
        URL in = ProcessorTest.class.getResource("top250nl_Hoogte.xml");
        TopNLType type = TopNLType.TOP250NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, type);
        assertEquals(1, hoogte.size());
    }
}
