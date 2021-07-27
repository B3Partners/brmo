/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.gml.stream.XmlStreamGeometryReader;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static nl.b3p.brmo.bgt.loader.BGTSchema.fixUUID;

public class BGTObjectStreamer implements Iterable<BGTObject> {
    private static final Log log = LogFactory.getLog(BGTObjectStreamer.class);

    private static final String NS_IMGEO = "http://www.geostandaarden.nl/imgeo/2.1";
    private static final String NS_CITYGML = "http://www.opengis.net/citygml/2.0";
    private static final String NS_GML = "http://www.opengis.net/gml";
    private static final String NS_MUTATIELEVERING = "http://www.kadaster.nl/schemas/mutatielevering-generiek/2.0";
    private static final String NS_MUTATIELEVERING_BGT = "http://www.kadaster.nl/schemas/mutatielevering-bgt/1.0";

    private static final QName CITYGML_CITY_OBJECT_MEMBER = new QName(NS_CITYGML, "cityObjectMember");
    private static final QName CITYGML_CITY_MODEL = new QName(NS_CITYGML, "CityModel");

    private static final QName MUTATIE_BERICHT = new QName(NS_MUTATIELEVERING, "mutatieBericht");

    private static final QName BGT_MUTATIES = new QName(NS_MUTATIELEVERING_BGT, "bgtMutaties");
    private static final QName BGT_OBJECT = new QName(NS_MUTATIELEVERING_BGT, "bgtObject");

    private static final QName LOKAAL_ID = new QName(NS_IMGEO, "lokaalID");

    private static final int SRID = 28992;

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cursor;

    private boolean isMutaties;
    private boolean hasMutatieGroep = false;

    private MutatieInhoud mutatieInhoud;

    public static class MutatieInhoud {
        private String mutatieType;
        private String gebied;
        private String leveringsId;
        private final List<String> objectTypen = new ArrayList<>();

        public String getMutatieType() {
            return mutatieType;
        }

        public String getGebied() {
            return gebied;
        }

        public String getLeveringsId() {
            return leveringsId;
        }

        public List<String> getObjectTypen() {
            return objectTypen;
        }
    }

    public MutatieInhoud getMutatieInhoud() {
        return mutatieInhoud;
    }

    public BGTObjectStreamer(File f) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(f));
        this.geometryReader = buildGeometryReader();
    }

    public BGTObjectStreamer(InputStream in) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(in));
        this.geometryReader = buildGeometryReader();
    }

    public BGTObjectStreamer(Reader r) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(r));
        this.geometryReader = buildGeometryReader();
    }

    private SMInputCursor initCursor(SMInputCursor cursor) throws XMLStreamException {
        QName root = cursor.advance().getQName();

        if (root.equals(CITYGML_CITY_MODEL)) {
            isMutaties = false;
        } else if (root.equals(BGT_MUTATIES)) {
            isMutaties = true;
        } else {
            throw new IllegalArgumentException("XML root element moet CityModel of bgtMutaties zijn");
        }

        // Note: when using StaxMate childElementCursor() or similar, only an local name string element does not work
        // when using Aalto. Always use a QName parameter!

        if(isMutaties) {
            cursor = cursor.childElementCursor(MUTATIE_BERICHT).advance().childElementCursor().advance();
            label:
            do {
                switch (cursor.getLocalName()) {
                    case "dataset":
                        assert cursor.collectDescendantText().equals("bgt");
                        break;
                    case "inhoud":
                        this.mutatieInhoud = parseInhoud(cursor);
                        break;
                    case "mutatieGroep":
                        // cursor positioned correctly
                        hasMutatieGroep = true;
                        break label;
                    default:
                        throw new IllegalStateException("Verwacht mutatieGroep element, gevonden " + cursor.getQName());
                }
            } while(cursor.getNext() != null);

        } else {
            cursor = cursor.childElementCursor(CITYGML_CITY_OBJECT_MEMBER).advance();
        }

        return cursor;
    }

    private static MutatieInhoud parseInhoud(SMInputCursor inhoudCursor) throws XMLStreamException {
        SMInputCursor cursor = inhoudCursor.childElementCursor();
        MutatieInhoud mutatieInhoud = new MutatieInhoud();
        while(cursor.getNext() != null) {
            switch(cursor.getLocalName()) {
                case "mutatieType":
                    mutatieInhoud.mutatieType = cursor.collectDescendantText().trim();
                    break;
                case "gebied":
                    mutatieInhoud.gebied = cursor.collectDescendantText().trim();
                    break;
                case "leveringsId":
                    mutatieInhoud.leveringsId = cursor.collectDescendantText().trim();
                    break;
                case "objectTypen":
                    SMInputCursor c = cursor.childElementCursor();
                    while (c.getNext() != null) {
                        mutatieInhoud.objectTypen.add(c.collectDescendantText().trim());
                    }
                    break;
            }
        }
        return mutatieInhoud;
    }

    protected XmlStreamGeometryReader buildGeometryReader() {
        return new XmlStreamGeometryReader(this.cursor.getStreamReader());
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

    @Override
    public Iterator<BGTObject> iterator() {
        return new Iterator<>() {
            SMEvent event = cursor.getCurrEvent();

            /**
             * A parse action may return multiple objects. Buffer the future objects to be returned in a next() call.
             */
            private final Queue<BGTObject> buffer = new LinkedList<>();

            @Override
            public boolean hasNext() {
                if (isMutaties && !hasMutatieGroep) {
                    return false;
                }
                if (!buffer.isEmpty()) {
                    return true;
                }
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
            public BGTObject next() {
                try {
                    if (!buffer.isEmpty()) {
                        return buffer.remove();
                    }
                    if (isMutaties && !hasMutatieGroep) {
                        throw new IllegalStateException("No items");
                    }
                    if (event == null) {
                        if(!hasNext()) {
                            throw new IllegalStateException("No more items");
                        }
                    }
                    // Make sure cityObjectMembers.getNext() is called with next Iterator.getNext() call
                    event = null;

                    Map<String, Object> attributes = new HashMap<>();

                    SMInputCursor cityObjectMemberChild;

                    String gmlIdPreviousVersion = null;
                    BGTObject.MutatieStatus mutatieStatus = BGTObject.MutatieStatus.WORDT;

                    if (isMutaties) {
                        // Assume single child per <mutatieGroep>, cursor is currently at <mutatieGroep>

                        SMInputCursor mutatie = cursor.childElementCursor().advance();
                        String mutatieNaam = mutatie.getLocalName();
                        if (mutatieNaam.equals("wijziging") || mutatieNaam.equals("toevoeging")) {
                            mutatie = mutatie.childElementCursor().advance();
                        } else {
                            throw new IllegalStateException("Ongeldig mutatieGroep child element: " + mutatie.getQName());
                        }

                        if (mutatieNaam.equals("wijziging")) {
                            assert mutatie.getLocalName().equals("was");
                            gmlIdPreviousVersion = fixUUID(mutatie.getAttrValue("id"));

                            if (mutatie.getNext() == null) {
                                // To support deletes, we would need to parse the <cityObjectMember> child element name
                                // at least, but deletes do not occur for bgt
                                throw new IllegalStateException("Mutaties 'was' zonder 'wordt' worden niet ondersteund voor BGT");
                            }
                            assert mutatie.getLocalName().equals("wordt");
                            mutatieStatus = BGTObject.MutatieStatus.WAS_WORDT;
                        } else if(mutatieNaam.equals("toevoeging")) {
                            assert mutatie.getLocalName().equals("wordt");
                            // mutatieStatus is already IMGeoObject.MutatieStatus.WORDT
                        } else {
                            throw new IllegalStateException("Ongeldig mutatie element: " + mutatie.getLocalName());
                        }

                        // Move cursor from <wordt> to <cityObjectMember> child
                        cityObjectMemberChild = mutatie.childElementCursor(BGT_OBJECT).advance()
                                .childElementCursor(CITYGML_CITY_OBJECT_MEMBER).advance()
                                .childElementCursor().advance();
                    } else {
                        cityObjectMemberChild = cursor.childElementCursor().advance();
                    }

                    final String name = cityObjectMemberChild.getLocalName();
                    final BGTSchema.BGTObjectType bgtObjectType = BGTSchema.getObjectTypeByName(name);
                    if (bgtObjectType == null) {
                        throw new IllegalArgumentException("Onbekend object type: " + name);
                    }
                    final Location location = cityObjectMemberChild.getCursorLocation();
                    attributes.put("gmlId", fixUUID(cityObjectMemberChild.getAttrValue(NS_GML, "id")));

                    SMInputCursor attributesCursor = cityObjectMemberChild.childElementCursor();

                    // Only a single collection attribute is supported (no matrix of multiple collection attributes)
                    // An IMGeoObject will be returned for each collection item
                    Collection<Map<String,Object>> collectionAttribute = null;

                    while (attributesCursor.getNext() != null) {
                        String attributeName = attributesCursor.getLocalName();
                        Object parsedAttribute = parseIMGeoAttribute(attributesCursor);

                        if (parsedAttribute instanceof BGTObject) {
                            List<BGTObject> oneToMany = (List<BGTObject>)attributes.get(attributeName);
                            if (oneToMany == null) {
                                oneToMany = new ArrayList<>();
                                attributes.put(attributeName, oneToMany);
                            }
                            oneToMany.add((BGTObject) parsedAttribute);
                        } else if (parsedAttribute instanceof Collection) {
                            if (collectionAttribute != null) {
                                throw new IllegalStateException("Only a single collection attribute is supported");
                            }
                            collectionAttribute = (Collection<Map<String, Object>>) parsedAttribute;
                        } else {
                            attributes.put(attributeName, parsedAttribute);
                        }
                    }

                    if (collectionAttribute != null && !collectionAttribute.isEmpty()) {
                        int index = 0;
                        for(Map<String,Object> collectionItem: collectionAttribute) {
                             Map<String,Object> attributesForCollectionItem = new HashMap<>(attributes);
                             attributesForCollectionItem.putAll(collectionItem);
                             attributesForCollectionItem.put(BGTSchema.INDEX, index++);
                             buffer.add(new BGTObject(bgtObjectType, attributesForCollectionItem, location));
                        }
                        return buffer.remove();
                    } else {
                        return new BGTObject(bgtObjectType, attributes, location, mutatieStatus, gmlIdPreviousVersion);
                    }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private Object parseIMGeoAttribute(SMInputCursor attribute) throws XMLStreamException, FactoryException, IOException {
        if("true".equals(attribute.getAttrValue("http://www.w3.org/2001/XMLSchema-instance", "nil"))) {
            return null;
        }

        if (attribute.hasLocalName("identificatie")) {
            /*
            <imgeo:identificatie>
                <imgeo:NEN3610ID>
                    <imgeo:namespace>NL.IMGeo</imgeo:namespace>
                    <imgeo:lokaalID>G1234.abcdef01234567890abcdef012345678</imgeo:lokaalID>
                </imgeo:NEN3610ID>
            </imgeo:identificatie>
             */
            // Just get the UUID
            String id = attribute.descendantElementCursor(LOKAAL_ID).advance().collectDescendantText().trim();
            // UUID is allowed to have '-' characters, normalize it to UUID without
            return id.replaceAll("-", "");
        }

        if (isGeometryElement(attribute.getLocalName())) {
            // Position underlying XmlStreamReader at GML element
            attribute.childElementCursor().advance();

            Geometry geom = geometryReader.readGeometry();
            geom.setSRID(SRID);
            return geom;
        }

        if (attribute.hasLocalName("nummeraanduidingreeks")) {
            Location location = attribute.getCursorLocation();
            Map<String,Object> nummeraanduidingreeks = parseNummeraanduidingreeks(attribute);
            return new BGTObject(BGTSchema.getObjectTypeByName("nummeraanduidingreeks"), nummeraanduidingreeks, location);
        }

        if(attribute.hasLocalName("openbareRuimteNaam")) {
            return parseOpenbareRuimteNaam(attribute);
        }

        // String attribute value as default
        return attribute.collectDescendantText().trim();
    }

    private static boolean isGeometryElement(String localName) {
        return localName.startsWith("geometrie") || localName.startsWith("kruinlijn");
    }

    private static final QName CAPITALIZED_NUMMERAANDUIDINGREEKS = new QName(NS_IMGEO, "Nummeraanduidingreeks");
    private static final QName LOWERCASE_NUMMERAANDUIDINGREEKS = new QName(NS_IMGEO, "nummeraanduidingreeks");
    private static final QName LABEL = new QName(NS_IMGEO, "Label");
    private static final QName LABELPOSITIE = new QName(NS_IMGEO, "Labelpositie");

    private Map<String,Object> parseNummeraanduidingreeks(SMInputCursor cursor) throws XMLStreamException, FactoryException, IOException {
        Map<String,Object> label = new HashMap<>();

        cursor = cursor.childElementCursor(CAPITALIZED_NUMMERAANDUIDINGREEKS).advance().childElementCursor();
        while (cursor.getNext() != null) {
            if (cursor.getQName().equals(LOWERCASE_NUMMERAANDUIDINGREEKS)) {
                SMInputCursor labelChilds = cursor.childElementCursor(LABEL).advance().childElementCursor();
                while(labelChilds.getNext() != null) {
                    if (labelChilds.hasLocalName("tekst")) {
                        label.put("tekst", labelChilds.collectDescendantText().trim());
                    } else if (labelChilds.hasLocalName("positie")) {
                        SMInputCursor labelPositie = labelChilds.childElementCursor(LABELPOSITIE).advance();

                        SMInputCursor labelPositieChilds = labelPositie.childElementCursor();
                        while(labelPositieChilds.getNext() != null) {
                            if (labelPositieChilds.hasLocalName("plaatsingspunt")) {
                                // Positioneer op <gml:Point/>
                                labelPositieChilds.childElementCursor().advance();
                                labelPositieChilds.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_GML, "Point");
                                Geometry geom = geometryReader.readGeometry();
                                geom.setSRID(SRID);
                                label.put("plaatsingspunt", geom);
                            } else if (labelPositieChilds.hasLocalName("hoek")) {
                                String hoek = labelPositieChilds.collectDescendantText().trim();
                                label.put("hoek", Double.parseDouble(hoek));
                            }
                        }
                    }
                }
            } else  {
                label.put(cursor.getLocalName(), cursor.collectDescendantText().trim());
            }
        }

        return label;
    }

    private List<Map<String,Object>> parseOpenbareRuimteNaam(SMInputCursor cursor) throws XMLStreamException, FactoryException, IOException {

        cursor = cursor.childElementCursor(LABEL).advance().childElementCursor();

        List<Map<String,Object>> posities = new ArrayList<>();
        String tekst = null;

        while (cursor.getNext() != null) {
            if (cursor.getLocalName().equals("tekst")) {
                tekst = cursor.collectDescendantText().trim();
            } else if(cursor.getLocalName().equals("positie")) {
                Map<String,Object> positie = new HashMap<>();
                positie.put("tekst", tekst);
                posities.add(positie);
                SMInputCursor positieCursor = cursor.childElementCursor().advance().childElementCursor();
                while (positieCursor.getNext() != null) {
                    if (positieCursor.getLocalName().equals("plaatsingspunt")) {
                        positieCursor.childElementCursor().advance();
                        Geometry geom = geometryReader.readGeometry();
                        geom.setSRID(SRID);
                        positie.put("plaatsingspunt", geom);
                    } else {
                        positie.put(positieCursor.getLocalName(), positieCursor.collectDescendantText().trim());
                    }
                }
            }
        }

        return posities;
    }
}
