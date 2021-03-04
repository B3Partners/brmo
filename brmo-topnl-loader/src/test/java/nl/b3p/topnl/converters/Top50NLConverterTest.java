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

import nl.b3p.topnl.Processor;
import nl.b3p.topnl.TestUtil;
import nl.b3p.topnl.TopNLType;
import nl.b3p.topnl.entities.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author meine
 */
public class Top50NLConverterTest extends TestUtil{

    private final Top50NLConverter instance;
    private final Processor processor;

    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

    public Top50NLConverterTest() throws JAXBException, SQLException {
        this.processor = new Processor(null);
        this.instance = new Top50NLConverter();
    }

    @Test
    public void testConvertNoFeatureCollection() throws Exception {
        Hoogte hoogte = new Hoogte();
        URL in = Top50NLConverterTest.class.getResource("top50nl/Hoogte.xml");
        List jaxb = processor.parse(in);

        List<TopNLEntity> expResult = Collections.singletonList(hoogte);
        List<TopNLEntity> result = instance.convert(jaxb);
        assertNotNull(result);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).getClass(), result.get(0).getClass());
    }

    /**
     * Test of convertHoogte method, of class Top50NLConverter.
     */
    @Test
    public void testConvertHoogte() throws Exception {
        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setVisualisatieCode(17320L);

        expected.setIdentificatie("NL.TOP50NL.18R11-0004284128");
        TopNLEntity entity = getEntity("top50nl/Hoogte.xml");

        assertNotNull(entity);
        assertTrue(entity instanceof Hoogte);

        testTopNLEntity(entity, entity);
        Hoogte h = (Hoogte) entity;

        assertEquals("hoogtelijn", h.getTypeHoogte());
        assertEquals(0d, h.getHoogte());
        assertNotNull(h.getGeometrie());
        assertEquals(LineString.class, h.getGeometrie().getClass());
    }

    @Test
    public void testConvertFunctioneelGebied() throws Exception {
        TopNLEntity entity = getEntity("top50nl/FunctioneelGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP50NL.18R11-0000140177");

        expected.setVisualisatieCode(15300L);

        assertNotNull(entity);
        assertTrue(entity instanceof FunctioneelGebied);

        FunctioneelGebied real = (FunctioneelGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("begraafplaats", real.getTypeFunctioneelGebied());
        assertEquals(Point.class, real.getGeometrie().getClass());
    }

    @Test
    @Disabled("GeografischGebied niet in TOP50NL")
    public void testConvertGeografischGebied() throws Exception {
        TopNLEntity entity = getEntity("top50nl/GeografischGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.16R11-0000084248");
        expected.setVisualisatieCode(48190L);

        assertNotNull(entity);
        assertTrue(entity instanceof GeografischGebied);

        GeografischGebied real = (GeografischGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals("zeegat, zeearm", real.getTypeGeografischGebied());
        assertEquals("Waddenzee", real.getNaamNL());
        assertEquals(Point.class, real.getGeometrie().getClass());
    }
    
    @Test
    public void testConvertGebouw() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Gebouw.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0005220160");
        expected.setVisualisatieCode(0L);

        assertNotNull(entity);
        assertTrue(entity instanceof Gebouw);

        Gebouw real = (Gebouw) entity;

        testTopNLEntity(expected, real);
        assertEquals(Point.class, real.getGeometrie().getClass());
        assertEquals( "overig", real.getTypeGebouw());
        assertEquals( "laagbouw", real.getHoogteklasse());
    }
    
       
    @Test
    public void testConvertInrichtingselement() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Inrichtingselement.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0005378256");
        expected.setVisualisatieCode(14810L);

        assertNotNull(entity);
        assertTrue(entity instanceof Inrichtingselement);

        Inrichtingselement real = (Inrichtingselement) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "hoogspanningsleiding", real.getTypeInrichtingselement());
        assertEquals( "onbekend", real.getStatus());
        assertEquals(0L, real.getHoogteniveau());
    }

    @Test
    @Disabled("Plaats niet in TOP50NL")
    public void testConvertPlaats() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Plaats.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.16R11-0000079618");
        expected.setVisualisatieCode(48110L);

        assertNotNull(entity);
        assertTrue(entity instanceof Plaats);

        Plaats real = (Plaats) entity;

        testTopNLEntity(expected, real);
        assertEquals(Point.class, real.getGeometrie().getClass());
        assertEquals( "woonkern", real.getTypeGebied());
        assertEquals( "Aldegea", real.getNaamFries());
    }
       
    @Test
    public void testConvertRegistratiefGebied() throws Exception {
        TopNLEntity entity = getEntity("top50nl/RegistratiefGebied.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0000150727");
        expected.setVisualisatieCode(18040L);

        assertNotNull(entity);
        assertTrue(entity instanceof RegistratiefGebied);

        RegistratiefGebied real = (RegistratiefGebied) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals( "provincie", real.getTypeRegistratiefGebied());
    }

    @Test
    public void testConvertRelief() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Relief.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0006900882");
        expected.setVisualisatieCode(17220L);

        assertNotNull(entity);
        assertTrue(entity instanceof Relief);

        Relief real = (Relief) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals( "talud, hoogteverschil", real.getTypeRelief());
        assertEquals( "> 2,5 meter", real.getHoogteklasse());
        assertEquals(0L, real.getHoogteniveau());
    }
    
    @Test
    public void testConvertSpoorbaandeel() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Spoorbaandeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();
        expected.setIdentificatie("NL.TOP50NL.18R11-0007042323");

        expected.setVisualisatieCode(14000L);

        assertNotNull(entity);
        assertTrue(entity instanceof Spoorbaandeel);

        Spoorbaandeel real = (Spoorbaandeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("trein", real.getTypeSpoorbaan());
        assertEquals("in gebruik", real.getStatus());
        assertEquals("1", real.getAantalSporen());
        assertEquals(0L, real.getHoogteniveau());
        // assertEquals("op vast deel van brug", real.getFysiekVoorkomen());
    }
    
    @Test
    public void testConvertTerrein() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Terrein.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0006219447");
        expected.setVisualisatieCode(15260L);

        assertNotNull(entity);
        assertTrue(entity instanceof Terrein);

        Terrein real = (Terrein) entity;

        testTopNLEntity(expected, real);
        assertEquals(Polygon.class, real.getGeometrie().getClass());
        assertEquals("overig", real.getTypeLandgebruik());
    }
    
    @Test
    public void testConvertWaterdeel() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Waterdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0008340134");
        expected.setVisualisatieCode(16010L);

        assertNotNull(entity);
        assertTrue(entity instanceof Waterdeel);

        Waterdeel real = (Waterdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("waterloop", real.getTypeWater());
        assertEquals("0,5 - 3 meter", real.getBreedteklasse());
        assertEquals(0L, real.getHoogteniveau());
    }
    
    @Test
    public void testConvertWegdeel() throws Exception {
        TopNLEntity entity = getEntity("top50nl/Wegdeel.xml");

        TopNLEntity expected = getStandardTestTopNLEntity();

        expected.setIdentificatie("NL.TOP50NL.18R11-0002239389");
        expected.setVisualisatieCode(13300L);

        assertNotNull(entity);
        assertTrue(entity instanceof Wegdeel);

        Wegdeel real = (Wegdeel) entity;

        testTopNLEntity(expected, real);
        assertEquals(LineString.class, real.getGeometrie().getClass());
        assertEquals("verbinding", real.getTypeInfrastructuur());
        assertEquals("regionale weg", real.getTypeWeg());
        assertEquals("gemengd verkeer", real.getHoofdverkeersgebruik());
        assertEquals(false, real.getGescheidenRijbaan());
        assertEquals("verhard", real.getVerhardingstype());
        assertEquals(0L, real.getHoogteniveau());
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
    top50nl:FunctioneelGebied
    top50nl:Hoogte
    top50nl:Gebouw
    top50nl:Inrichtingselement
    top50nl:Plaats
    top50nl:GeografischGebied
    
    top50nl:RegistratiefGebied
    top50nl:Relief
    top50nl:Spoorbaandeel
    top50nl:Terrein
    top50nl:Waterdeel
    top50nl:Wegdeel

     */
    private TopNLEntity getEntity(String file) throws JAXBException, IOException, SAXException, ParserConfigurationException, TransformerException {
        URL in = Top50NLConverterTest.class.getResource(file);
        List jaxb = processor.parse(in);
        return instance.convertObject(jaxb.get(0));
    }
    
      
    public TopNLEntity getStandardTestTopNLEntity() throws ParseException {
        TopNLEntity expected = new TopNLEntity() {};

        expected.setTopnltype(TopNLType.TOP50NL.getType());
        expected.setBrontype("TOP10NL");
        expected.setBronactualiteit(sdf.parse("2018-11-01"));
        expected.setBronbeschrijving("Automatische generalisatie vanuit TOP10NL");
        expected.setObjectBeginTijd(sdf.parse("2018-11-01"));
        
        return expected;
    }
}
