/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2Object;
import nl.b3p.brmo.bag2.schema.BAG2ObjectType;
import nl.b3p.brmo.bag2.schema.BAG2Schema;
import nl.b3p.brmo.util.Force2DCoordinateSequenceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.gml.stream.XmlStreamGeometryReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BAG2GMLObjectStream implements Iterable<BAG2Object> {
    private static final Log log = LogFactory.getLog(BAG2GMLObjectStream.class);

    private static final String NS_BAG_EXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";
    private static final String NS_STANDLEVERING = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    private static final String NS_GML_32 = "http://www.opengis.net/gml/3.2";
    private static final String NS_HISTORIE = "www.kadaster.nl/schemas/lvbag/imbag/historie/v20200601";
    private static final String NS_OBJECTEN_REF = "www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20200601";

    private static final int SRID = 28992;

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cursor;

    public BAG2GMLObjectStream(InputStream in) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(in));
        this.geometryReader = buildGeometryReader();
    }

    protected SMInputFactory buildSMInputFactory() {
        // Using alternative StAX parsers explicitly:
        // final XMLInputFactory stax = new WstxInputFactory(); // Woodstox
        // final XMLInputFactory stax = new com.fasterxml.aalto.stax.InputFactoryImpl(); // Aalto
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory(); // JRE Default, depends on JAR's present or javax.xml.stream.XMLInputFactory property, can be SJSXP
        log.trace("StAX XMLInputFactory: " + xmlInputFactory.getClass().getName());

        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE); // Coalesce characters
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE); // No XML entity expansions or external entities

        return new SMInputFactory(xmlInputFactory);
    }

    protected XmlStreamGeometryReader buildGeometryReader() {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 28992, new Force2DCoordinateSequenceFactory());
        return new XmlStreamGeometryReader(this.cursor.getStreamReader(), geometryFactory);
    }

    private SMInputCursor initCursor(SMInputCursor cursor) throws XMLStreamException {
        QName root = cursor.advance().getQName();

        if (!root.equals(new QName(NS_BAG_EXTRACT, "bagStand"))) {
            throw new IllegalArgumentException("XML root element moet bagStand zijn");
        }

        cursor = cursor.childElementCursor(new QName(NS_STANDLEVERING, "standBestand")).advance()
                .childElementCursor(new QName(NS_STANDLEVERING, "stand"));
        return cursor;
    }

    @Override
    public Iterator<BAG2Object> iterator() {
        return new Iterator<>() {
            SMEvent event = cursor.getCurrEvent();

            @Override
            public boolean hasNext() {
                if (event != null) {
                    return true;
                }
                try {
                    event = cursor.getNext();
                    return event != null;
                } catch(XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public BAG2Object next() {
                if (event == null) {
                    if(!hasNext()) {
                        throw new IllegalStateException("No more items");
                    }
                }
                // Make sure cursor.getNext() is called in a future next() call
                event = null;

                try {
                    SMInputCursor bagObjectCursor = cursor.childElementCursor(new QName(NS_BAG_EXTRACT, "bagObject")).advance().childElementCursor().advance();

                    String name = bagObjectCursor.getLocalName();

                    final BAG2ObjectType objectType = BAG2Schema.getInstance().getObjectTypeByName(name);
                    if (objectType == null) {
                        throw new IllegalArgumentException("Onbekend object type: " + name);
                    }

                    Map<String, Object> attributes = new HashMap<>();
                    SMInputCursor attributeCursor = bagObjectCursor.childElementCursor();
                    while (attributeCursor.getNext() != null) {
                        parseAttribute(attributeCursor, attributes);
                    }
                    return new BAG2Object(objectType, attributes);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private void parseAttribute(SMInputCursor attribute, Map<String, Object> objectAttributes) throws XMLStreamException, FactoryException, IOException {
        String attributeName = attribute.getLocalName();

        if (attributeName.equals("geometrie")) {
            // Position cursor at child element
            SMInputCursor geomCursor = attribute.childElementCursor().advance();
            // Sometimes there is an element like "punt" which has the actual geometry as child element
            if (!geomCursor.getNsUri().equals(NS_GML_32)) {
                geomCursor.childElementCursor().advance();
            }
            Geometry geom = geometryReader.readGeometry();
            geom.setSRID(SRID);
            objectAttributes.put(attributeName, geom);
        } else if (attributeName.equals("voorkomen")) {
            // Flatten al Voorkomen child attributes, according to the schema the element names do not conflict
            parseVoorkomen(attribute, objectAttributes);
        } else if (attributeName.equals("BeschikbaarLV")) {
            // Flatten al Voorkomen/BeschikbaarLV child attributes to the top level, according to the schema the element
            // names do not conflict
            parseBeschikbaarLV(attribute, objectAttributes);
        } else if(attributeName.equals("heeftAlsNevenadres")) {
            parseNevenadres(attribute, objectAttributes);
        } else if(attributeName.equals("maaktDeelUitVan")) {
            parseMaaktDeelUitVan(attribute, objectAttributes);
        } else if(attributeName.equals("gebruiksdoel")) {
            parseGebruiksdoel(attribute, objectAttributes);
        } else {
            // String attribute value as default

            // This also works for ligtIn en ligtAan
            objectAttributes.put(attributeName, attribute.collectDescendantText().trim());
        }
    }

    private void parseVoorkomen(SMInputCursor attributeCursor, Map<String, Object> objectAttributes) throws XMLStreamException, FactoryException, IOException {
        attributeCursor = attributeCursor.childElementCursor(new QName(NS_HISTORIE, "Voorkomen")).advance().childElementCursor();
        while (attributeCursor.getNext() != null) {
            parseAttribute(attributeCursor, objectAttributes);
        }
    }

    private void parseBeschikbaarLV(SMInputCursor attributeCursor, Map<String, Object> objectAttributes) throws XMLStreamException, FactoryException, IOException {
        attributeCursor = attributeCursor.childElementCursor();
        while (attributeCursor.getNext() != null) {
            parseAttribute(attributeCursor, objectAttributes);
        }
    }

    private void parseNevenadres(SMInputCursor attributeCursor, Map<String, Object> objectAttributes) throws XMLStreamException {
        Set<String> values = new HashSet<>();
        attributeCursor = attributeCursor.childElementCursor(new QName(NS_OBJECTEN_REF, "NummeraanduidingRef"));
        while (attributeCursor.getNext() != null) {
            values.add(attributeCursor.collectDescendantText().trim());
        }
        objectAttributes.put("heeftAlsNevenadres", values);
    }

    private void parseMaaktDeelUitVan(SMInputCursor attributeCursor, Map<String, Object> objectAttributes) throws XMLStreamException {
        Set<String> values = new HashSet<>();
        attributeCursor = attributeCursor.childElementCursor(new QName(NS_OBJECTEN_REF, "PandRef"));
        while (attributeCursor.getNext() != null) {
            values.add(attributeCursor.collectDescendantText().trim());
        }
        objectAttributes.put("maaktDeelUitVan", values);
    }

    private void parseGebruiksdoel(SMInputCursor attributeCursor, Map<String, Object> objectAttributes) throws XMLStreamException {
        Set<String> values = (Set<String>) objectAttributes.get("gebruiksdoel");
        if (values == null) {
            values = new HashSet<>();
            objectAttributes.put("gebruiksdoel", values);
        }
        values.add(attributeCursor.collectDescendantText().trim());
    }
}
