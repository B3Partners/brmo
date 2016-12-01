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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

/**
 *
 * @author meine
 */
public class ProcessorTest extends TestUtil{
    
    private Processor instance;
    
    public ProcessorTest() {
    }

    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws JAXBException {
        instance = new Processor(datasource);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class Processor.
     */
    @Test
    public void testParse() throws JAXBException {
        System.out.println("parse");
        InputStream in = ProcessorTest.class.getResourceAsStream("top250nl_Hoogte.xml");
        Object jaxb = instance.parse(in);
        assertNotNull(jaxb);
        assertTrue (jaxb instanceof nl.b3p.topnl.top250nl.FeatureCollectionT250NLType);
    }


    /**
     * Test of save method, of class Processor.
     */
    @Test
    public void testSave() throws JAXBException, IOException, SAXException, TransformerException, ParserConfigurationException, SQLException {
        System.out.println("save");
        InputStream in = ProcessorTest.class.getResourceAsStream("top250nl_Hoogte.xml");
        
        Object jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, TopNLType.TOP250NL);
        instance.save(hoogte.get(0), TopNLType.TOP250NL);
        QueryRunner run = new QueryRunner(datasource);

        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class);

        Hoogte real = run.query("SELECT * FROM top250nl.Hoogte WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000084246");
        assertNotNull(real);
    }

    /**
     * Test of convert method, of class Processor.
     */
    @Test
    public void testConvert() throws Exception {
        System.out.println("convert");
        InputStream in = ProcessorTest.class.getResourceAsStream("top250nl_Hoogte.xml");
        Object jaxb = instance.parse(in);
        List<TopNLEntity> hoogte = instance.convert(jaxb, TopNLType.TOP250NL);
        assertEquals(1, hoogte.size());
    }
    
}
