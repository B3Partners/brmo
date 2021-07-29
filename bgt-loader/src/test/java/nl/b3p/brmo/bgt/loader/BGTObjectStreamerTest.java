/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import org.geotools.geometry.jts.WKTReader2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import javax.xml.stream.XMLStreamException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BGTObjectStreamerTest {

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void testAllFeatureTypes(String file, String objectName, Object[][] attributes) throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(BGTTestFiles.getTestInputStream(file));
        Iterator<BGTObject> iterator = streamer.iterator();
        BGTObject object = iterator.next();
        assertNotNull(object);
        assertEquals(objectName, object.getObjectType().getName());
        Map<String,Object> objectAttributes = object.getAttributes();
        //System.out.println(objectAttributes);
        Set<String> allKeys = new HashSet<>(objectAttributes.keySet());
        for(Object[] attribute: attributes) {
            String key = (String) attribute[0];
            Object value = attribute[1];
            assertTrue(objectAttributes.containsKey(key), "Attribute should exist: " + key);
            Object objectValue = objectAttributes.get(key);
            assertEquals(value, objectValue, "Attribute equals: " + key);
            allKeys.remove(key);
        }
        assertEquals(0, allKeys.size(), "Unexpected attributes: " + allKeys);
    }

    private static Stream<Arguments> testAllFeatureTypes() throws ParseException {
        return Stream.of(Arguments.of("bgt_stadsdeel.gml", "Stadsdeel", new Object[][] {
                {"bronhouder","G0213"},
                {"identificatie","G0213.3242ce08ec434e728f6380d7f2ec88cc"},
                {"bgt-status","bestaand"},
                {"tijdstipRegistratie","2019-03-03T11:05:27.000"},
                {"inOnderzoek","false"},
                {"creationDate","2019-03-03"},
                {"LV-publicatiedatum","2019-03-04T10:11:48"},
                {"terminationDate","2019-03-20"},
                {"relatieveHoogteligging","0"},
                {"plus-status","geenWaarde"},
                {"geometrie2d", new WKTReader().read("MULTIPOLYGON (((207264.493 454955.512, 207264.189 454954.828, 207266.772 454953.005, 207267.987 454954.296, 207264.493 454955.512)))")},
                {"eindRegistratie","2019-03-20T15:05:03"},
                {"gmlId","0d8fd67b6e453f9def31e26be9ebcbfe"},
        }), Arguments.of("bgt_waterschap.gml", "Waterschap", new Object[][] {
                {"LV-publicatiedatum", "2019-03-04T10:11:48"},
                {"bgt-status", "bestaand"},
                {"bronhouder", "G0213"},
                {"creationDate", "2019-03-03"},
                {"eindRegistratie","2019-03-20T15:06:08"},
                {"geometrie2d", new WKTReader().read("MULTIPOLYGON (((207269.886 454951.106, 207267.987 454948.371, 207268.823 454947.688, 207270.266 454949.89, 207269.886 454951.106)))")},
                {"gmlId","d8095fd0b8dee285a0e6a2a4980c132a"},
                {"identificatie","G0213.9a788699c5734dd4a8c44b3d54cba16c"},
                {"inOnderzoek","false"},
                {"plus-status","geenWaarde"},
                {"relatieveHoogteligging","0"},
                {"terminationDate","2019-03-20"},
                {"tijdstipRegistratie","2019-03-03T11:05:44.000"},
        }), Arguments.of("bgt_wijk_curve.gml", "Wijk", new Object[][] {
                {"LV-publicatiedatum", "2019-12-19T14:36:54"},
                {"bgt-status", "bestaand"},
                {"bronhouder", "G0321"},
                {"creationDate", "2019-12-06"},
                {"geometrie2d", new WKTReader2().read("MULTISURFACE (CURVEPOLYGON (COMPOUNDCURVE ((143636.1 446084.69, 143629.332 446075.836, 143619.982 446062.76, 143607.362 446045.206, 143597.43 446031.012, 143585.849 446015.01, 143579.538 446006.563, 143577.991 446005.137, 143576.443 446004.22, 143574.717 446003.658, 143572.836 446003.628, 143571.514 446003.931, 143563.348 446009.046, 143549.989 446017.519, 143544.933 446009.502, 143551.566 446005.365, 143560.112 446000.0, 143560.724 445999.616, 143567.797 445995.064, 143576.942 445989.235, 143579.708 445987.472, 143594.865 445977.852, 143607.697 445969.683, 143619.43 445962.064, 143631.213 445954.623, 143639.426 445949.294, 143645.119 445945.601, 143659.027 445936.324, 143673.42 445926.901, 143683.826 445919.956, 143687.531 445917.611, 143689.399 445916.428, 143694.651 445912.881, 143692.544 445909.78, 143693.422 445909.17, 143694.342 445908.541, 143637.358 445825.195, 143638.267 445824.515, 143627.466 445809.052, 143667.756 445778.857, 143825.974 445523.971, 143952.382 445442.112, 143973.132 445481.887, 143982.632 445500.0, 143991.353 445516.853, 144000.0 445533.564, 144005.263 445544.741, 144008.652 445551.288, 144004.326 445556.374, 144000.0 445561.46, 143992.271 445571.037, 143986.989 445577.504, 143998.161 445586.063, 144005.843 445576.428, 144012.217 445568.429, 144020.54 445558.01, 144027.724 445549.018, 144034.503 445540.382, 144041.535 445531.441, 144048.819 445522.118, 144056.533 445512.183, 144065.495 445500.625, 144155.931 445621.106, 144246.836 445742.211, 144060.227 445875.528, 144136.154 445976.656, 144128.931 445977.178, 144121.708 445977.701, 144115.063 445978.505, 144108.543 445979.291, 144109.469 445985.775, 144091.334 445988.654, 144088.079 445989.266, 144083.921 445990.049, 144095.332 446006.214, 144103.68 446018.242, 144105.188 446020.248, 143991.687 446114.474, 143986.054 446106.908, 143848.792 446212.333, 143815.811 446167.998, 143786.521 446128.623, 143757.25 446088.883, 143743.279 446090.3, 143720.956 446092.283, 143698.616 446093.12, 143687.434 446093.657, 143655.339 446094.736, 143652.581 446094.511), CIRCULARSTRING (143652.581 446094.511, 143643.345 446091.27, 143636.1 446084.69))))")},
                {"gmlId","137b4e48feb841bc98933c23bae42e44"},
                {"identificatie","G0321.3dc14768e70b40b8a837ee91348769b6"},
                {"inOnderzoek","false"},
                {"plus-status","geenWaarde"},
                {"relatieveHoogteligging","0"},
                {"tijdstipRegistratie","2019-12-06T08:17:27.000"},
                {"naam", "'t Goy"},
                {"wijkcode", "032130"}
        }));
    }

    @Test
    void testOpenbareRuimteLabel() throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(BGTTestFiles.getTestInputStream("bgt_openbareruimtelabel.gml"));
        Iterator<BGTObject> iterator = streamer.iterator();
        GeometryFactory gf = new GeometryFactory();
        Point[] punten = new Point[] {
                gf.createPoint(new Coordinate(19993.166, 365316.442)),
                gf.createPoint(new Coordinate(19968.671, 365117.131)),
                gf.createPoint(new Coordinate(19944.305, 364918.490)),
                gf.createPoint(new Coordinate(19931.877, 364785.079)),
        };
        String[] hoeken = new String[] {
                "-82.9",
                "-83.1",
                "-82.9",
                "-82.8",
        };
        for(int i = 0; i < punten.length; i++) {
            BGTObject object = iterator.next();
            assertEquals("OpenbareRuimteLabel", object.getObjectType().getName());
            assertNotNull(object);
            assertEquals(i, object.getAttributes().get("idx"));
            assertEquals(hoeken[i], object.getAttributes().get("hoek"));
        }
        AtomicInteger count = new AtomicInteger(punten.length);
        iterator.forEachRemaining(o -> count.incrementAndGet());
        assertEquals(20, count.get());
    }

    @Test
    void testMutatieInhoud() throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(BGTTestFiles.getTestInputStream("bgt_mutatie_delta_pand.xml"));
        BGTObjectStreamer.MutatieInhoud mutatieInhoud = streamer.getMutatieInhoud();
        assertNotNull(mutatieInhoud);
        assertEquals("delta", mutatieInhoud.getMutatieType());
        assertEquals("POLYGON ((127813 461266, 141440 461266, 141440 447884, 127953 447884, 127813 461266))", mutatieInhoud.getGebied());
        assertEquals("c3791a07-b388-42a6-94eb-494ea4baa3b8", mutatieInhoud.getLeveringsId());
        assertNotNull(mutatieInhoud.getObjectTypen());
        assertArrayEquals(new String[]{"pand"}, mutatieInhoud.getObjectTypen().toArray());
    }

    @Test
    void testMutatiePand() throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(BGTTestFiles.getTestInputStream("bgt_mutatie_delta_pand.xml"));
        Iterator<BGTObject> iterator = streamer.iterator();
        BGTObject object = iterator.next();
        assertNotNull(object);
        assertEquals("BuildingPart", object.getObjectType().getName());
        assertEquals(BGTObject.MutatieStatus.WAS_WORDT, object.getMutatieStatus());
        assertEquals("ad402cde39e2fb79590fad390e6b78a5", object.getMutatiePreviousVersionGmlId());
        Map<String,Object> attributes = object.getAttributes();
        assertEquals("a318eb7108fd639cfe5ebfc36b723aec", attributes.get("gmlId"));
        assertEquals("G0335.4c7766d48a82474abafc59d41707a232", attributes.get("identificatie"));
        assertNotNull(attributes.get("geometrie2dGrondvlak"));
        assertTrue(attributes.get("geometrie2dGrondvlak") instanceof MultiPolygon);
        MultiPolygon polygon = (MultiPolygon)attributes.get("geometrie2dGrondvlak");
        assertEquals(28992, polygon.getSRID());
        assertEquals(1, polygon.getNumGeometries());
        Polygon p = (Polygon) polygon.getGeometryN(0);
        assertEquals(0, p.getNumInteriorRing());
        assertEquals("LINEARRING (127439.592 452184.313, 127427.892 452186.396, 127426.384 452177.925, 127438.084 452175.843, 127439.592 452184.313)", p.getExteriorRing().toString());
        assertNotNull(attributes.get("nummeraanduidingreeks"));
        assertTrue(attributes.get("nummeraanduidingreeks") instanceof List);
        List<BGTObject> nummeraanduidingreeksen = (List<BGTObject>) attributes.get("nummeraanduidingreeks");
        assertEquals(1, nummeraanduidingreeksen.size());
        object = nummeraanduidingreeksen.get(0);
        assertEquals("nummeraanduidingreeks", object.getObjectType().getName());
        assertEquals(object.getAttributes(), Stream.of(new Object[][] {
                {"identificatieBAGVBOLaagsteHuisnummer", "0335010000327253"},
                {"plaatsingspunt", new GeometryFactory().createPoint(new Coordinate(127434.037, 452182.942))},
                {"tekst", "27"},
                {"hoek", 11.1},
        }).collect(Collectors.toMap(e -> (String)e[0], e -> e[1])));

        object = iterator.next();
        assertNotNull(object);
        assertEquals("BuildingPart", object.getObjectType().getName());
        assertEquals(BGTObject.MutatieStatus.WORDT, object.getMutatieStatus());
        assertEquals("6c729d86ce928280e0168412f3923c90", object.getAttributes().get("gmlId"));
    }

    @Test
    void testMutatiesEmpty() throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(BGTTestFiles.getTestInputStream("bgt_mutatie_empty.xml"));
        BGTObjectStreamer.MutatieInhoud mutatieInhoud = streamer.getMutatieInhoud();
        assertNotNull(mutatieInhoud);
        assertFalse(streamer.iterator().hasNext());

    }
}