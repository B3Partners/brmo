package nl.b3p.brmo.bgt.loader;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BGTObjectStreamerTest {

    private InputStream getTestFile(String name) {
        InputStream input = this.getClass().getResourceAsStream("/nl/b3p/brmo/bgt/loader/" + name);
        assertNotNull(input);
        return input;
    }

    @Test
    void testMutatieInhoud() throws XMLStreamException {
        BGTObjectStreamer streamer = new BGTObjectStreamer(getTestFile("bgt_mutaties_pand.xml"));
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
        BGTObjectStreamer streamer = new BGTObjectStreamer(getTestFile("bgt_mutaties_pand.xml"));
        BGTObject object = streamer.iterator().next();
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
    }
}