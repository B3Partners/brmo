/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2Object;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BAG2GMLMutatieGroepStreamTest {

    @Test
    @Disabled
    void testMaandmutaties() throws Exception {
        try(FileInputStream in = new FileInputStream("extracten.bag.kadaster.nl/lvbag/extracten/Nederland maandmutaties/9999MUT08062021-08072021-000004_fo.xml")) {
            BAG2GMLMutatieGroepStream stream = new BAG2GMLMutatieGroepStream(in);

            assertNotNull(stream.getBagInfo());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            assertEquals(df.parse("2021-07-08"), stream.getBagInfo().getStandTechnischeDatum());
            assertEquals(df.parse("2021-06-08"), stream.getBagInfo().getMutatieDatumVanaf());
            assertEquals(df.parse("2021-07-08"), stream.getBagInfo().getMutatieDatumTot());

            Iterator<BAG2MutatieGroep> iterator = stream.iterator();
            BAG2MutatieGroep mutatieGroep = iterator.next();

            assertNotNull(mutatieGroep);
            int count = 1;
            while (iterator.hasNext()) {
                mutatieGroep = iterator.next();

                if (mutatieGroep.getMutaties().size() > 2) {
                    System.out.printf("mutatieGroep size %d at %s\n", mutatieGroep.getMutaties().size(), mutatieGroep.getMutaties().get(0).getLocation());
                }
                count++;
            }
            assertEquals(10000, count);
        }
    }

    @Test
    void testMutaties() throws Exception {
        ZipInputStream zip = new ZipInputStream(BAG2TestFiles.getTestInputStream("1978MUT15082021-15092021-000002.zip"));
        zip.getNextEntry();
        BAG2GMLMutatieGroepStream stream = new BAG2GMLMutatieGroepStream(zip);

        assertNotNull(stream.getBagInfo());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(df.parse("2021-09-15"), stream.getBagInfo().getStandTechnischeDatum());
        assertEquals(df.parse("2021-08-15"), stream.getBagInfo().getMutatieDatumVanaf());
        assertEquals(df.parse("2021-09-15"), stream.getBagInfo().getMutatieDatumTot());

        Iterator<BAG2MutatieGroep> iterator = stream.iterator();
        BAG2MutatieGroep mutatieGroep = iterator.next();

        assertNotNull(mutatieGroep);

        assertEquals(2, mutatieGroep.getMutaties().size());

        BAG2Mutatie mutatie = mutatieGroep.getMutaties().get(0);
        assertInstanceOf(BAG2WijzigingMutatie.class, mutatie);
        assertNull(((BAG2WijzigingMutatie)mutatie).getWas().getAttributes().get("eindGeldigheid"));
        assertEquals("2021-08-16", ((BAG2WijzigingMutatie)mutatie).getWordt().getAttributes().get("eindGeldigheid"));

        mutatie = mutatieGroep.getMutaties().get(1);
        assertInstanceOf(BAG2ToevoegingMutatie.class, mutatie);

        BAG2Object obj = ((BAG2ToevoegingMutatie)mutatie).getToevoeging();
        String identificatie = (String)obj.getAttributes().get("identificatie");
        assertEquals("1978100007698683", identificatie);

        mutatieGroep = iterator.next();
        assertNotNull(mutatieGroep);
        mutatieGroep = iterator.next();
        assertNotNull(mutatieGroep);
        assertTrue(mutatieGroep.isSingleToevoeging());

        int count = 3;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(847, count);
    }

    @Test
    void testMutatieMultipleWijzigingen() throws Exception {
        // A mutatieGroep is not always wijziging+toevoeging or only toevoeging. There may be multiple wijziging
        // elements, test these are parsed correctly

        try(InputStream in = BAG2TestFiles.getTestInputStream("mut-multiple-wijziging.xml")) {
            BAG2GMLMutatieGroepStream stream = new BAG2GMLMutatieGroepStream(in);

            assertNotNull(stream.getBagInfo());

            Iterator<BAG2MutatieGroep> iterator = stream.iterator();
            BAG2MutatieGroep mutatieGroep = iterator.next();

            assertEquals(3, mutatieGroep.getMutaties().size());
            assertInstanceOf(BAG2WijzigingMutatie.class, mutatieGroep.getMutaties().get(0));
            assertInstanceOf(BAG2WijzigingMutatie.class, mutatieGroep.getMutaties().get(1));
            assertInstanceOf(BAG2ToevoegingMutatie.class, mutatieGroep.getMutaties().get(2));

            mutatieGroep = iterator.next();
            assertNotNull(mutatieGroep);
            assertFalse(iterator.hasNext());
        }
    }

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void testAllFeatureTypes(String file, String objectName, Object[][] expectedObjects) throws XMLStreamException {
        BAG2GMLMutatieGroepStream stream = new BAG2GMLMutatieGroepStream(BAG2TestFiles.getTestInputStream(file));
        assertNotNull(stream.getBagInfo().getStandTechnischeDatum());
        assertEquals(Collections.singleton("9999"), stream.getBagInfo().getGemeenteIdentificaties());
        Iterator<BAG2MutatieGroep> iterator = stream.iterator();
        int objectNum = 1;
        for(Object expectedObj: expectedObjects) {
            Object[][] attributes = (Object[][])expectedObj;
            BAG2MutatieGroep mutatieGroep = iterator.next();
            assertNotNull(mutatieGroep);
            BAG2Object object = mutatieGroep.getSingleToevoeging();
            assertEquals(objectName, object.getObjectType().getName());
            Map<String,Object> objectAttributes = object.getAttributes();
            //System.out.println(objectAttributes);
            Set<String> allKeys = new HashSet<>(objectAttributes.keySet());
            for(Object[] attribute: attributes) {
                String key = (String) attribute[0];
                Object value = attribute[1];
                assertTrue(objectAttributes.containsKey(key), "Object #" + objectNum + ": Attribute should exist: " + key);
                Object objectValue = objectAttributes.get(key);
                assertEquals(value, objectValue, "Object #" + objectNum + ": Attribute equals: " + key);
                allKeys.remove(key);
            }
            assertEquals(0, allKeys.size(), "Object #" + objectNum + ": Unexpected attributes: " + allKeys);
            objectNum++;
        }
    }

    @Test
    public void testPandIs2D() throws Exception {
        // BAG Pand have a Z coordinate, but this is always 0 and not meaningful. Test that the class removes the Z
        // coordinate. When writing to Oracle Spatial we need to drop the Z or create the index with XYZ. Currently a
        // XY index is hardcoded in OracleDialect.

        BAG2GMLMutatieGroepStream stream = new BAG2GMLMutatieGroepStream(BAG2TestFiles.getTestInputStream("stand-pnd.xml"));
        Iterator<BAG2MutatieGroep> iterator = stream.iterator();
        BAG2Object object = iterator.next().getSingleToevoeging();
        assertNotNull(object);
        Geometry geometry = (Geometry)object.getAttributes().get("geometrie");
        assertNotNull(geometry);
        Coordinate coordinate = geometry.getCoordinate();
        assertNotNull(coordinate);
        assertTrue(Double.isNaN(coordinate.getZ()));
    }

    private static Stream<Arguments> testAllFeatureTypes() throws ParseException {
        return Stream.of(Arguments.of("stand-num.xml", "Nummeraanduiding", new Object[][] {
            new Object[][]{
                {"identificatie", "1987200000003195"},
                {"huisnummer", "38"},
                {"postcode", "9649BW"},
                {"typeAdresseerbaarObject", "Verblijfsobject"},
                {"status", "Naamgeving uitgegeven"},
                {"geconstateerd", "N"},
                {"documentdatum", "2010-04-20"},
                {"documentnummer", "430-2010"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2010-04-20"},
                {"tijdstipRegistratie", "2010-10-21T11:28:59.000"},
                {"tijdstipRegistratieLV", "2010-10-25T10:02:30.782"},
                {"ligtAan", "1987300000000027"},
        }, new Object[][]{
                {"identificatie", "0003200000140540"},
                {"huisnummer", "33"},
                {"huisletter", "b"},
                {"postcode", "9902RM"},
                {"typeAdresseerbaarObject", "Verblijfsobject"},
                {"status", "Naamgeving uitgegeven"},
                {"geconstateerd", "N"},
                {"documentdatum", "2012-12-20"},
                {"documentnummer", "A2012-WFS-007B"},
                {"voorkomenidentificatie", "4"},
                {"beginGeldigheid", "2012-12-20"},
                {"eindGeldigheid", "2013-02-11"},
                {"tijdstipRegistratie", "2012-12-20T14:50:19.000"},
                {"eindRegistratie", "2013-02-11T08:25:01.000"},
                {"tijdstipRegistratieLV", "2012-12-20T15:05:25.600"},
                {"tijdstipEindRegistratieLV", "2013-02-11T08:31:21.583"},
                {"ligtAan", "0010300000000346"},
                {"ligtIn","3386"},
        }}),Arguments.of("stand-lig.xml", "Ligplaats", new Object[][] {
            new Object[][]{
                {"heeftAlsHoofdadres", "0431200000003413"},
                {"identificatie", "0431020000000018"},
                {"geometrie", new WKTReader().read("POLYGON ((119965.936 495377.184, 119954.426 495373.197, 119956.133 495368.268, 119967.643 495372.255, 119965.936 495377.184))")},
                {"status", "Plaats aangewezen"},
                {"geconstateerd", "N"},
                {"documentdatum", "2009-09-15"},
                {"documentnummer", "09-3972"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2009-09-15"},
                {"tijdstipRegistratie", "2010-11-18T21:33:08.000"},
                {"tijdstipRegistratieLV", "2010-11-18T22:01:14.316"},
            }, new Object[][]{
                {"heeftAlsHoofdadres", "0513200000029675"},
                {"heeftAlsNevenadres", Stream.of("0513200000029676", "0513200000041054").collect(Collectors.toSet())},
                {"geometrie", new WKTReader().read("POLYGON ((108533.738 446703.649, 108532.252 446701.688, 108538.676 446696.014, 108550.842 446685.144, 108550.906 446685.206, 108551.027 446685.103, 108551.072 446684.977, 108551.034 446684.854, 108550.906 446684.669, 108551.87 446684.335, 108552.52 446684.214, 108553.352 446684.145, 108554.193 446684.153, 108554.863 446684.243, 108556.35 446684.561, 108557.105 446684.786, 108557.634 446684.994, 108558.146 446685.246, 108558.839 446685.662, 108559.454 446686.117, 108565.769 446689.67, 108572.389 446693.566, 108576.795 446695.749, 108581.08 446697.016, 108589.555 446698.762, 108591.095 446700.389, 108591.713 446701.359, 108579.853 446702.516, 108553.24 446707.795, 108535.056 446705.14, 108533.738 446703.649))")},
                {"identificatie", "0513020000000044"},
                {"status", "Plaats aangewezen"},
                {"geconstateerd", "N"},
                {"documentdatum", "2013-09-16"},
                {"documentnummer", "BAG108"},
                {"voorkomenidentificatie", "2"},
                {"beginGeldigheid", "2013-09-16"},
                {"tijdstipRegistratie", "2013-09-16T14:49:42.000"},
                {"tijdstipRegistratieLV", "2013-09-16T15:02:15.754"},
            }
        }),Arguments.of("stand-sta.xml", "Standplaats", new Object[][] {
            new Object[][]{
                {"heeftAlsHoofdadres", "1884200000047583"},
                {"identificatie", "1884030000012658"},
                {"geometrie", new WKTReader().read("POLYGON ((104082.641 468561.449, 104085.394 468562.007, 104083.664 468570.233, 104080.91 468569.65, 104082.641 468561.449))")},
                {"status", "Plaats aangewezen"},
                {"geconstateerd", "N"},
                {"documentdatum", "2010-09-10"},
                {"documentnummer", "10.13520"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2010-09-10"},
                {"eindGeldigheid", "2011-10-11"},
                {"tijdstipRegistratie", "2010-12-28T14:54:08.000"},
                {"eindRegistratie", "2011-10-12T12:55:41.000"},
                {"tijdstipRegistratieLV", "2010-12-28T16:22:08.839"},
                {"tijdstipEindRegistratieLV", "2011-10-12T13:01:49.433"},
            }, new Object[][]{
                {"heeftAlsHoofdadres", "1921200002054078"},
                {"heeftAlsNevenadres", Stream.of("1921200002054079").collect(Collectors.toSet())},
                {"identificatie", "1921030002054080"},
                {"geometrie", new WKTReader().read("POLYGON ((181055.997 560924.816, 181073.938 560930.19, 181066.477 560952.584, 181049.241 560947.355, 181055.997 560924.816))")},
                {"status", "Plaats aangewezen"},
                {"geconstateerd", "N"},
                {"documentdatum", "2014-07-10"},
                {"documentnummer", "07.155.2 10-07-14SP1"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2014-07-10"},
                {"eindGeldigheid", "2014-07-10"},
                {"tijdstipRegistratie", "2014-07-11T13:13:51.000"},
                {"eindRegistratie", "2014-07-11T13:31:41.000"},
                {"tijdstipRegistratieLV", "2014-07-11T13:31:05.570"},
                {"tijdstipEindRegistratieLV", "2014-07-11T14:01:00.618"},
            }
        }),Arguments.of("stand-wpl.xml", "Woonplaats", new Object[][]{
            new Object[][]{
                {"identificatie", "2663"},
                {"geometrie", new WKTReader().read("POLYGON ((71508.318 450055.265, 71058.558 449616.07, 71171.939 449494.361, 71656.064 449905.23, 71508.318 450055.265))")},
                {"naam", "Ter Heijde"},
                {"status", "Woonplaats aangewezen"},
                {"geconstateerd", "N"},
                {"documentdatum", "2009-06-10"},
                {"documentnummer", "09-0022652"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2009-06-10"},
                {"tijdstipRegistratie", "2010-07-02T12:16:22.000"},
                {"tijdstipRegistratieLV", "2010-07-05T15:56:57.417"},
            }
        }),Arguments.of("stand-opr.xml", "OpenbareRuimte", new Object[][]{
            new Object[][]{
                {"identificatie", "1987300000000027"},
                {"naam", "Burgemeester Buitenhofstraat"},
                {"verkorteNaam","Burg Buitenhofstraat"},
                {"ligtIn", "2278"},
                {"type", "Weg"},
                {"status", "Naamgeving uitgegeven"},
                {"geconstateerd", "N"},
                {"documentdatum", "2010-04-20"},
                {"documentnummer", "437-2010"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2010-04-20"},
                {"tijdstipRegistratie", "2010-10-21T11:03:13.000"},
                {"tijdstipRegistratieLV", "2010-10-25T10:00:57.584"},
            }
        }),Arguments.of("stand-pnd.xml", "Pand", new Object[][]{
            new Object[][]{
                {"identificatie", "1987100000011083"},
                {"geometrie", new WKTReader().read("POLYGON ((253319.961 576037.087, 253319.295 576043.281, 253317.51 576043.089, 253318.176 576036.895, 253319.961 576037.087))")},
                {"oorspronkelijkBouwjaar", "2012"},
                {"status", "Pand gesloopt"},
                {"geconstateerd", "N"},
                {"documentdatum", "2021-01-18"},
                {"documentnummer", "D2021-01-002570"},
                {"voorkomenidentificatie", "2"},
                {"beginGeldigheid", "2021-01-18"},
                {"tijdstipRegistratie", "2021-01-18T13:34:17.230"},
                {"tijdstipRegistratieLV", "2021-01-18T13:42:59.738"},
            }
        }),Arguments.of("stand-vbo.xml", "Verblijfsobject", new Object[][]{
            new Object[][]{
                {"heeftAlsHoofdadres", "0000200000057534"},
                {"identificatie", "0000010000057469"},
                {"geometrie", new WKTReader().read("POINT (188391.884 334586.439 0.0)")},
                {"status", "Verblijfsobject in gebruik"},
                {"gebruiksdoel", Stream.of("woonfunctie").collect(Collectors.toSet())},
                {"maaktDeelUitVan", Stream.of("1883100000010452").collect(Collectors.toSet())},
                {"oppervlakte", "72"},
                {"geconstateerd", "N"},
                {"documentdatum", "2018-03-26"},
                {"documentnummer", "BV05.00043-HLG"},
                {"voorkomenidentificatie", "1"},
                {"beginGeldigheid", "2018-03-26"},
                {"eindGeldigheid", "2018-04-04"},
                {"tijdstipRegistratie", "2018-03-26T11:37:36.000"},
                {"eindRegistratie", "2018-04-04T11:59:28.000"},
                {"tijdstipRegistratieLV", "2018-03-26T12:00:43.223"},
                {"tijdstipEindRegistratieLV", "2018-04-04T12:01:06.236"},
            }, new Object[][] {
                {"heeftAlsHoofdadres", "0003200000134083"},
                {"identificatie", "0003010000126023"},
                {"geometrie", new WKTReader().read("POINT (253064.76 593662.242)")},
                {"status", "Verblijfsobject in gebruik"},
                {"gebruiksdoel", Stream.of("woonfunctie", "winkelfunctie").collect(Collectors.toSet())},
                {"maaktDeelUitVan", Stream.of("0003100000118445").collect(Collectors.toSet())},
                {"oppervlakte", "141"},
                {"geconstateerd", "J"},
                {"documentdatum", "2011-07-15"},
                {"documentnummer", "2011/APVC001"},
                {"voorkomenidentificatie", "2"},
                {"beginGeldigheid", "2011-07-15"},
                {"eindGeldigheid", "2011-07-15"},
                {"tijdstipRegistratie", "2011-07-15T12:08:05.000"},
                {"eindRegistratie", "2011-07-15T13:15:01.000"},
                {"tijdstipRegistratieLV", "2011-07-15T12:32:28.116"},
                {"tijdstipEindRegistratieLV", "2011-07-15T13:32:58.426"},
            }, new Object[][] {
                {"heeftAlsHoofdadres", "0010200000109774"},
                {"heeftAlsNevenadres", Stream.of("1979200000000293").collect(Collectors.toSet())},
                {"identificatie", "0010010000059610"},
                {"geometrie", new WKTReader().read("POINT (257064.0 593220.0)")},
                {"status", "Verblijfsobject in gebruik"},
                {"gebruiksdoel", Stream.of("industriefunctie").collect(Collectors.toSet())},
                {"maaktDeelUitVan", Stream.of("0010100000006543").collect(Collectors.toSet())},
                {"oppervlakte", "3651"},
                {"geconstateerd", "N"},
                {"documentdatum", "2021-04-09"},
                {"documentnummer", "E2021-BNU-086"},
                {"voorkomenidentificatie", "4"},
                {"beginGeldigheid", "2021-04-09"},
                {"tijdstipRegistratie", "2021-04-09T11:49:26.672"},
                {"tijdstipRegistratieLV", "2021-04-15T10:59:51.205"},
            }
        })
        );
    }
}