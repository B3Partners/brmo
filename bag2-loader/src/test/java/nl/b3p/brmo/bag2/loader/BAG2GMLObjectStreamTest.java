/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2Object;
import nl.b3p.brmo.bgt.loader.BGTObjectStreamer;
import nl.b3p.brmo.bgt.schema.BGTObject;
import org.junit.jupiter.api.Disabled;
import org.locationtech.jts.io.ParseException;

import javax.xml.stream.XMLStreamException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BAG2GMLObjectStreamTest {

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    @Disabled
    void testAllFeatureTypes(String file, String objectName, Object[][] attributes) throws XMLStreamException {
        BAG2GMLObjectStream stream = new BAG2GMLObjectStream(BAG2TestFiles.getTestInputStream(file));
        Iterator<BAG2Object> iterator = stream.iterator();
        BAG2Object object = iterator.next();
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
        return Stream.of(Arguments.of("stand-num.xml", "Nummeraanduiding", new Object[][]{
                {"identificatie", "1987200000003195"},
                {"huisnummer", "38"},
                {"postcode", "9649BW"},
                {"typeAdresseerbaarObject", "Verblijfsobject"},
                {"status", "Naamgeving uitgegeven"},
                {"geconstateerd", "N"},
                {"documentdatum", "2010-04-20"},
                {"documentnummer", "430-2010"}
        }));
    }
}