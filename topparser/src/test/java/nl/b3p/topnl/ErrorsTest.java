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
import nl.b3p.topnl.entities.Terrein;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class ErrorsTest  extends TestUtil{
    
    private Processor instance;
    public ErrorsTest(){
        this.useDB = true;
    }
    
    @Before
    public void before() throws JAXBException, SQLException{
        instance = new Processor(datasource);
    }
    
    
    @Test
    public void polygonErrors1Test() throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException, ParseException, SQLException {
        URL in = ErrorsTest.class.getResource("problems/polygonError1.xml");
        TopNLType type = TopNLType.TOP250NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> terrein = instance.convert(jaxb, type);
        instance.save(terrein.get(0), type);

        ResultSetHandler<Terrein> handler = new BeanHandler<>(Terrein.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Terrein real = run.query("SELECT * FROM top250nl.terrein WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000064537");
        assertNotNull(real);
    }
    
    
    @Test
    public void polygonErrors2Test() throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException, ParseException, SQLException {
        URL in = ErrorsTest.class.getResource("problems/polygonError2.xml");
        TopNLType type = TopNLType.TOP250NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> terrein = instance.convert(jaxb, type);
        instance.save(terrein.get(0), type);

        ResultSetHandler<Terrein> handler = new BeanHandler<>(Terrein.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Terrein real = run.query("SELECT * FROM top250nl.terrein WHERE identificatie=?", handler, "NL.TOP250NL.16R09-0000064539");
        assertNotNull(real);
    }
    
    @Test
    public void spoorbaanDePunt() throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException, ParseException, SQLException {
        URL in = ErrorsTest.class.getResource("problems/SpoorDePunt.xml");
        TopNLType type = TopNLType.TOP10NL;
        List jaxb = instance.parse(in);
        List<TopNLEntity> spoorbaandeel = null;
        try{
             spoorbaandeel = instance.convert(jaxb, type);
        }catch(ClassCastException e){
            fail("Cannot convert spoorbaandeel with point geometry type");
        }
        assertEquals(1,spoorbaandeel.size());
        instance.save(spoorbaandeel.get(0), type);

        ResultSetHandler<Terrein> handler = new BeanHandler<>(Terrein.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(GeometryJdbcConverterFactory.getGeometryJdbcConverter(datasource.getConnection()))));

        Terrein real = run.query("SELECT * FROM top10nl.spoorbaandeel WHERE identificatie=?", handler, "NL.TOP10NL.105531727");
        assertNotNull(real);
    }
}
