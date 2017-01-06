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
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Top10NLConverterTest extends TestUtil{

    private final Top10NLConverter instance;
    private final Processor processor;

    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public Top10NLConverterTest() throws JAXBException, SQLException {
        this.processor = new Processor(null);
        this.instance = new Top10NLConverter();
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testConvertNoFeatureCollection() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException {
        System.out.println("convert");
        Hoogte hoogte = new Hoogte();
        URL in = Top10NLConverterTest.class.getResource("top10nl/Hoogte.xml");
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
        System.out.println("convert");
        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setBronnauwkeurigheid(20000.0);
        expected.setBronbeschrijving("Externe data: Hoogtegegevens. Gebaseerd op hoogtemodel Kadaster dat is bijgehouden met behulp van stereo luchtfoto's");
        expected.setVisualisatieCode(new Long("16300"));

        expected.setIdentificatie("NL.TOP10NL.105441792");
        TopNLEntity entity = getEntity("top10nl/Hoogte.xml");

        assertNotNull(entity);
        assertTrue(entity instanceof Hoogte);

        testTopNLEntity(entity, entity);
        Hoogte h = (Hoogte) entity;
        
        assertEquals("hoogtepunt", h.getTypeHoogte());
        assertEquals(new Double(31.5), h.getHoogte());
        assertEquals("NAP", h.getReferentieVlak());
        assertNotNull(h.getGeometrie());
        assertEquals(Point.class, h.getGeometrie().getClass());
    }

    @Test
    public void testConvertFunctioneelGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertFunctioneelGebied");
        TopNLEntity entity = getEntity("top10nl/FunctioneelGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP10NL.100002175");

        expected.setVisualisatieCode(new Long("19450"));

        assertNotNull(entity);
        assertTrue(entity instanceof FunctioneelGebied);

        FunctioneelGebied real = (FunctioneelGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("sportterrein, sportcomplex", real.getTypeFunctioneelGebied());
        assertEquals(Polygon.class, real.getGeometrie().getClass());
    }

    // GeografischGebied niet in Top100NL
    @Test
    public void testConvertGeografischGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertGeografischGebied");
        TopNLEntity entity = getEntity("top10nl/GeografischGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.121199089");
        expected.setVisualisatieCode(new Long("18130"));

        assertNotNull(entity);
        assertTrue(entity instanceof GeografischGebied);

        GeografischGebied real = (GeografischGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("streek, veld", real.getTypeGeografischGebied());
        assertEquals("Luchter Zeeduinen", real.getNaamNL());
        assertEquals(Point.class, real.getGeometrie().getClass());
    }
    
    @Test
    public void testConvertGebouw() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertGebouw");
        TopNLEntity entity = getEntity("top10nl/Gebouw.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setBronactualiteit(sdf.parse("2013-01-01"));
        expected.setObjectBeginTijd(sdf.parse("2014-05-29"));

        expected.setIdentificatie("NL.TOP10NL.127584838");
        expected.setVisualisatieCode(new Long("13000"));
        expected.setBronnauwkeurigheid(new Double("100"));

        assertNotNull(entity);
        assertTrue(entity instanceof Gebouw);

        Gebouw real = (Gebouw) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals( "overig", real.getTypeGebouw());
        assertEquals( "laagbouw", real.getHoogteklasse());
        assertEquals( "in gebruik", real.getStatus());
    }
    
       
    @Test
    public void testConvertInrichtingselement() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertInrichtingselement");
        TopNLEntity entity = getEntity("top10nl/Inrichtingselement.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setBronactualiteit(sdf.parse("2006-01-01"));
        expected.setObjectBeginTijd(sdf.parse("2008-11-24"));

        expected.setIdentificatie("NL.TOP10NL.103869300");
        expected.setVisualisatieCode(new Long("15090"));

        assertNotNull(entity);
        assertTrue(entity instanceof Inrichtingselement);

        Inrichtingselement real = (Inrichtingselement) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "koedam", real.getTypeInrichtingselement());
        assertEquals(new Long("0"), real.getHoogteniveau());
    }
 
    // Plaats niet in Top100NL
    //@Test
    public void testConvertPlaats() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertPlaats");
        TopNLEntity entity = getEntity("top10nl/Plaats.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.16R11-0000079618");
        expected.setVisualisatieCode(new Long("48110"));

        assertNotNull(entity);
        assertTrue(entity instanceof Plaats);

        Plaats real = (Plaats) entity;

        testTopNLEntity(expected, real);
        assertEquals(Point.class, real.getGeometrie().getClass());
        assertEquals( "woonkern", real.getTypeGebied());
        assertEquals( "Aldegea", real.getNaamFries());
    }
       
    @Test
    public void testConvertRegistratiefGebied() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertRegistratiefGebied");
        TopNLEntity entity = getEntity("top10nl/RegistratiefGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.129704430");
        expected.setVisualisatieCode(new Long("17010"));

        assertNotNull(entity);
        assertTrue(entity instanceof RegistratiefGebied);

        RegistratiefGebied real = (RegistratiefGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals( "provincie", real.getTypeRegistratiefGebied());
        assertEquals( "31", real.getNummer());
        assertEquals( "Limburg", real.getNaamNL());
        assertEquals( "Limburg", real.getNaamOfficieel());
    }

    @Test
    public void testConvertRelief() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertRelief");
        TopNLEntity entity = getEntity("top10nl/Relief.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.105335729");
        expected.setVisualisatieCode(new Long("16400"));

        assertNotNull(entity);
        assertTrue(entity instanceof Relief);

        Relief real = (Relief) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "wal", real.getTypeRelief());
        assertEquals( "> 1 meter", real.getHoogteklasse());
        assertEquals(new Long("0"), real.getHoogteniveau());
    }
    
    @Test
    public void testConvertSpoorbaandeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertSpoorbaandeel");
        TopNLEntity entity = getEntity("top10nl/Spoorbaandeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP10NL.16R11-0001444636");

        expected.setVisualisatieCode(new Long("-11000"));

        assertNotNull(entity);
        assertTrue(entity instanceof Spoorbaandeel);

        Spoorbaandeel real = (Spoorbaandeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("metro", real.getTypeSpoorbaan());
        assertEquals("verbinding", real.getTypeInfrastructuur());
        assertEquals("normaalspoor", real.getSpoorbreedte());
        assertEquals("in gebruik", real.getStatus());
        assertEquals("enkel", real.getAantalSporen());
        assertEquals("personenvervoer", real.getVervoerfunctie());
        assertEquals(true, real.getElektrificatie());
        assertEquals(new Long("-1"), real.getHoogteniveau());
    }
    
    @Test
    public void testConvertTerrein() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertTerrein");
        TopNLEntity entity = getEntity("top10nl/Terrein.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.106687191");
        expected.setVisualisatieCode(new Long("14160"));

        assertNotNull(entity);
        assertTrue(entity instanceof Terrein);

        Terrein real = (Terrein) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals("overig", real.getTypeLandgebruik());
    }
    
    @Test
    public void testConvertWaterdeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertWaterdeel");
        TopNLEntity entity = getEntity("top10nl/Waterdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.109888370");
        expected.setVisualisatieCode(new Long("12100"));

        assertNotNull(entity);
        assertTrue(entity instanceof Waterdeel);

        Waterdeel real = (Waterdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("greppel, droge sloot", real.getTypeWater());
        assertEquals("overig", real.getFysiekVoorkomen());
        assertEquals(false, real.getGetijdeinvloed());
        assertEquals("overig", real.getVoorkomen());
        assertEquals(new Long("0"), real.getHoogteniveau());
    }
    
    @Test
    public void testConvertWegdeel() throws IOException, SAXException, ParserConfigurationException, TransformerException, JAXBException, ParseException {
        System.out.println("testConvertWegdeel");
        TopNLEntity entity = getEntity("top10nl/Wegdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP10NL.114492184");
        expected.setVisualisatieCode(new Long("10750"));

        assertNotNull(entity);
        assertTrue(entity instanceof Wegdeel);

        Wegdeel real = (Wegdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("verbinding", real.getTypeInfrastructuur());
        assertEquals("overig", real.getTypeWeg());
        assertEquals("voetgangers", real.getHoofdverkeersgebruik());
        assertEquals(false, real.getGescheidenRijbaan());
        assertEquals("onbekend", real.getVerhardingstype());
        assertEquals("Weijpoort", real.getNaam());
        assertEquals(new Long("0"), real.getHoogteniveau());
        assertEquals("in gebruik", real.getStatus());
        assertEquals(null, real.getAantalRijstroken());
        assertEquals("> 2 meter", real.getVerhardingsbreedteklasse());
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
    top10nl:FunctioneelGebied
    top10nl:Hoogte
    top10nl:Gebouw
    top10nl:Inrichtingselement
    top10nl:Plaats
    top10nl:GeografischGebied
    
    top10nl:RegistratiefGebied
    top10nl:Relief
    top10nl:Spoorbaandeel
    top10nl:Terrein
    top10nl:Waterdeel
    top10nl:Wegdeel

     */
    private TopNLEntity getEntity(String file) throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException {
        URL in = Top10NLConverterTest.class.getResource(file);
        List jaxb = processor.parse(in);
        TopNLEntity entity = instance.convertObject(jaxb.get(0));
        return entity;
    }
    
      
    public TopNLEntity getStandardTestTopNLEntity() throws ParseException {
        TopNLEntity expected = new TopNLEntity() {};

        expected.setTopnltype(TopNLType.TOP10NL.getType());
        expected.setBrontype("luchtfoto");
        expected.setBronactualiteit(sdf.parse("2015-01-01"));
        expected.setBronnauwkeurigheid(100.0);
        expected.setBronbeschrijving("Een orthogerectificeerde fotografische opname van een deel van het aardoppervlak. Gemaakt vanuit een vliegtuig.");
        expected.setObjectBeginTijd(sdf.parse("2008-11-24"));
        
        return expected;
    }
}
