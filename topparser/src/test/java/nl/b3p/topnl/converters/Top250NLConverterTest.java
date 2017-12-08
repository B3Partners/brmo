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
package nl.b3p.topnl.converters;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.topnl.Processor;
import nl.b3p.topnl.TestUtil;
import nl.b3p.topnl.TopNLType;
import nl.b3p.topnl.entities.FunctioneelGebied;
import nl.b3p.topnl.entities.Gebouw;
import nl.b3p.topnl.entities.GeografischGebied;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.Inrichtingselement;
import nl.b3p.topnl.entities.Plaats;
import nl.b3p.topnl.entities.RegistratiefGebied;
import nl.b3p.topnl.entities.Relief;
import nl.b3p.topnl.entities.Spoorbaandeel;
import nl.b3p.topnl.entities.Terrein;
import nl.b3p.topnl.entities.TopNLEntity;
import nl.b3p.topnl.entities.Waterdeel;
import nl.b3p.topnl.entities.Wegdeel;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author meine
 */
public class Top250NLConverterTest extends TestUtil{

    private final Top250NLConverter instance;
    private final Processor processor;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

    public Top250NLConverterTest() throws JAXBException, SQLException {
        this.processor = new Processor(null);
        this.instance = new Top250NLConverter();
    }

    /**
     * Test of convert method, of class Top250NLConverter.
     */
    @Test
    public void testConvertFeatureCollection() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException {
        Hoogte hoogte = new Hoogte();
        URL in = Top250NLConverterTest.class.getResource("top250nl/FeatureCollectionHoogte.xml");
        List jaxb = processor.parse(in);

        List<TopNLEntity> expResult = Collections.singletonList(hoogte);
        List<TopNLEntity> result = instance.convert(jaxb);
        assertNotNull(result);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).getClass(), result.get(0).getClass());
    }

    @Test
    public void testConvertNoFeatureCollection() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException {
        Hoogte hoogte = new Hoogte();
        URL in = Top250NLConverterTest.class.getResource("top250nl/Hoogte.xml");
        List jaxb = processor.parse(in);

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
    public void testConvertHoogte() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setVisualisatieCode(new Long("45550"));

        expected.setIdentificatie("NL.TOP250NL.17R11-0000060130");
        TopNLEntity entity = getEntity("top250nl/Hoogte.xml");

        assertNotNull(entity);
        assertTrue(entity instanceof Hoogte);

        testTopNLEntity(entity, entity);
        Hoogte h = (Hoogte) entity;

        assertEquals("hoogwaterlijn", h.getTypeHoogte());
        assertNotNull(h.getGeometrie());
        assertEquals(LineString.class, h.getGeometrie().getClass());
    }

    @Test
    public void testConvertFunctioneelGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/FunctioneelGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP250NL.17R11-0000002255");

        expected.setObjectBeginTijd(sdf.parse("2017-11-01"));
        expected.setVisualisatieCode(new Long("49500"));

        assertNotNull(entity);
        assertTrue(entity instanceof FunctioneelGebied);

        FunctioneelGebied real = (FunctioneelGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("verzorgingsplaats", real.getTypeFunctioneelGebied());
        assertEquals(Point.class, real.getGeometrie().getClass());
    }

    @Test
    public void testConvertGeografischGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/GeografischGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000063633");
        expected.setVisualisatieCode(new Long("48190"));

        assertNotNull(entity);
        assertTrue(entity instanceof GeografischGebied);

        GeografischGebied real = (GeografischGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("zeegat, zeearm", real.getTypeGeografischGebied());
        assertEquals("Zuiderhaaks", real.getNaamNL());
        assertEquals(Point.class, real.getGeometrie().getClass());
    }
    
    @Test
    public void testConvertGebouw() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Gebouw.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000000981");
        expected.setVisualisatieCode(new Long("43000"));

        assertNotNull(entity);
        assertTrue(entity instanceof Gebouw);

        Gebouw real = (Gebouw) entity;

        testTopNLEntity(expected, real);
        assertEquals(Point.class, real.getGeometrie().getClass());
        assertEquals("pompstation", real.getTypeGebouw());
        assertEquals( "in gebruik", real.getStatus());
    }
    
       
    @Test
    public void testConvertInrichtingselement() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Inrichtingselement.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000062222");
        expected.setVisualisatieCode(new Long("45650"));

        assertNotNull(entity);
        assertTrue(entity instanceof Inrichtingselement);

        Inrichtingselement real = (Inrichtingselement) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "strekdam, krib, golfbreker", real.getTypeInrichtingselement());
    }
 
       
    @Test
    public void testConvertPlaats() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Plaats.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000065006");
        expected.setVisualisatieCode(new Long("48110"));

        assertNotNull(entity);
        assertTrue(entity instanceof Plaats);

        Plaats real = (Plaats) entity;

        testTopNLEntity(expected, real);
        assertEquals(Point.class, real.getGeometrie().getClass());
        assertEquals( "woonkern", real.getTypeGebied());
        assertEquals(null, real.getNaamFries());
        assertEquals("Dreischor", real.getNaamNL());
    }
       
    @Test
    public void testConvertRegistratiefGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/RegistratiefGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000088408");
        expected.setVisualisatieCode(new Long("47010"));

        assertNotNull(entity);
        assertTrue(entity instanceof RegistratiefGebied);

        RegistratiefGebied real = (RegistratiefGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals("provincie", real.getTypeRegistratiefGebied());
        assertEquals("Utrecht", real.getNaamOfficieel());
    }

    @Test
    public void testConvertRelief() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Relief.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000063032");
        expected.setVisualisatieCode(new Long("46810"));

        assertNotNull(entity);
        assertTrue(entity instanceof Relief);

        Relief real = (Relief) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "dijk", real.getTypeRelief());
        assertEquals( "> 2,5 meter", real.getHoogteklasse());
    }
    
    @Test
    public void testConvertSpoorbaandeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Spoorbaandeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP250NL.17R11-0000056770");

        expected.setVisualisatieCode(new Long("41120"));

        assertNotNull(entity);
        assertTrue(entity instanceof Spoorbaandeel);

        Spoorbaandeel real = (Spoorbaandeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("verbinding", real.getTypeInfrastructuur());
        assertEquals("trein", real.getTypeSpoorbaan());
        assertEquals("normaalspoor", real.getSpoorbreedte());
        assertEquals("meervoudig", real.getAantalSporen());
        assertEquals("gemengd gebruik", real.getVervoerfunctie());
        assertEquals(true, real.getElektrificatie());
        assertEquals("in gebruik", real.getStatus());
    }
    
    @Test
    public void testConvertTerrein() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Terrein.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000060512");
        expected.setVisualisatieCode(new Long("44095"));

        assertNotNull(entity);
        assertTrue(entity instanceof Terrein);

        Terrein real = (Terrein) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals("bos", real.getTypeLandgebruik());
    }
    
    @Test
    public void testConvertWaterdeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Waterdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000070210");
        expected.setVisualisatieCode(new Long("42410"));

        assertNotNull(entity);
        assertTrue(entity instanceof Waterdeel);

        Waterdeel real = (Waterdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("waterloop", real.getTypeWater());
        assertEquals("< 6 meter", real.getBreedteklasse());
        assertEquals(false, real.getGetijdeinvloed());
        assertEquals("", real.getNaamNL());
    }
    
    @Test
    public void testConvertWegdeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        TopNLEntity entity = getEntity("top250nl/Wegdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP250NL.17R11-0000003186");
        expected.setVisualisatieCode(new Long("40510"));

        assertNotNull(entity);
        assertTrue(entity instanceof Wegdeel);

        Wegdeel real = (Wegdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("verbinding", real.getTypeInfrastructuur());
        assertEquals("lokale weg", real.getTypeWeg());
        assertEquals("gemengd verkeer", real.getHoofdverkeersgebruik());
        assertEquals(false, real.getGescheidenRijbaan());
        assertEquals("verhard", real.getVerhardingstype());
        assertEquals(new Long("2"), real.getAantalRijstroken());
        assertEquals("in gebruik", real.getStatus());
    }

    public void testTopNLEntity(TopNLEntity expected, TopNLEntity real) {
        assertEquals(expected.getTopnltype(), real.getTopnltype());

        assertEquals(expected.getId(), real.getId());
        assertEquals(expected.getIdentificatie(), real.getIdentificatie());
        assertEquals(expected.getBrontype(), real.getBrontype());
        assertEquals(expected.getBronactualiteit(), real.getBronactualiteit());
        assertEquals(expected.getBronbeschrijving(), real.getBronbeschrijving());
        assertEquals(expected.getBronnauwkeurigheid(), real.getBronnauwkeurigheid());
        assertEquals(expected.getObjectBeginTijd(), real.getObjectBeginTijd());
        assertEquals(expected.getObjectEindTijd(), real.getObjectEindTijd());
        assertEquals(expected.getVisualisatieCode(), real.getVisualisatieCode());
    }

    /*
    top250nl:FunctioneelGebied
    top250nl:Hoogte
    top250nl:Gebouw
    top250nl:Inrichtingselement
    top250nl:Plaats
    top250nl:GeografischGebied
    
    top250nl:RegistratiefGebied
    top250nl:Relief
    top250nl:Spoorbaandeel
    top250nl:Terrein
    top250nl:Waterdeel
    top250nl:Wegdeel

     */
    private TopNLEntity getEntity(String file) throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException {
        URL in = Top250NLConverterTest.class.getResource(file);
        List jaxb = processor.parse(in);
        TopNLEntity entity = instance.convertObject(jaxb.get(0));
        return entity;
    }
    
    
    
    public TopNLEntity getStandardTestTopNLEntity() throws ParseException {
        TopNLEntity expected = new TopNLEntity() {};

        expected.setTopnltype(TopNLType.TOP250NL.getType());
        expected.setBrontype("ERM");
        expected.setBronactualiteit(sdf.parse("2017-11-01"));
        expected.setBronbeschrijving("TOP50NL wordt als bron gebruikt bij het bijwerken van EuroRegionalMap (ERM). Via een automatisch proces worden de gegevens van ERM omgezet naar TOP250NL");
        expected.setBronnauwkeurigheid(125.0);
        expected.setObjectBeginTijd(sdf.parse("2017-11-01"));
        expected.setVisualisatieCode(1616L);
        
        return expected;
}
}
