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
import nl.b3p.brmo.bag2.util.Force2DCoordinateSequenceFactory;
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
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BAG2GMLMutatieGroepStream implements Iterable<BAG2MutatieGroep> {
    private static final Log log = LogFactory.getLog(BAG2GMLMutatieGroepStream.class);

    private static final String NS_BAG_EXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";
    private static final String NS_BAG_SELECTIES = "http://www.kadaster.nl/schemas/lvbag/extract-selecties/v20200601";
    private static final String NS_STANDLEVERING = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    private static final String NS_GML_32 = "http://www.opengis.net/gml/3.2";
    private static final String NS_HISTORIE = "www.kadaster.nl/schemas/lvbag/imbag/historie/v20200601";
    private static final String NS_OBJECTEN_REF = "www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20200601";
    private static final String NS_BAG_EXTRACT_MUTATIES = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-mutaties-lvc/v20200601";
    private static final String NS_MUTATIELEVERING = "http://www.kadaster.nl/schemas/mutatielevering-generiek/1.0";

    private static final QName BAG_STAND = new QName(NS_BAG_EXTRACT, "bagStand");
    private static final QName BAG_MUTATIES = new QName(NS_BAG_EXTRACT_MUTATIES, "bagMutaties");
    private static final int SRID = 28992;

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cursor;

    private boolean isMutaties;
    private boolean hasMutatieGroep;

    private BagInfo bagInfo;

    public static class BagInfo {
        private final Date standTechnischeDatum;
        private final Date mutatieDatumVanaf;
        private final Date mutatieDatumTot;
        private Set<String> gemeenteIdentificaties;

        protected BagInfo(Date standTechnischeDatum, Date mutatieDatumVanaf, Date mutatieDatumTot) {
            this(standTechnischeDatum, mutatieDatumVanaf, mutatieDatumTot, (String)null);
        }

        protected BagInfo(Date standTechnischeDatum, Date mutatieDatumVanaf, Date mutatieDatumTot, String gemeenteIdentificatie) {
            this(standTechnischeDatum, mutatieDatumVanaf, mutatieDatumTot, Collections.singleton(gemeenteIdentificatie));
        }

        protected BagInfo(Date standTechnischeDatum, Date mutatieDatumVanaf, Date mutatieDatumTot, Set<String> gemeenteIdentificaties) {
            this.standTechnischeDatum = standTechnischeDatum;
            this.mutatieDatumVanaf = mutatieDatumVanaf;
            this.mutatieDatumTot = mutatieDatumTot;
            this.gemeenteIdentificaties = gemeenteIdentificaties;
        }

        public Date getStandTechnischeDatum() {
            return standTechnischeDatum;
        }

        public Date getMutatieDatumVanaf() {
            return mutatieDatumVanaf;
        }

        public Date getMutatieDatumTot() {
            return mutatieDatumTot;
        }

        public Set<String> getGemeenteIdentificaties() {
            return gemeenteIdentificaties;
        }

        public void setGemeenteIdentificaties(Set<String> gemeenteIdentificaties) {
            this.gemeenteIdentificaties = gemeenteIdentificaties;
        }

        public boolean equalsExceptGemeenteIdentificaties(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BagInfo that = (BagInfo) o;
            return Objects.equals(standTechnischeDatum, that.standTechnischeDatum) && Objects.equals(mutatieDatumVanaf, that.mutatieDatumVanaf) && Objects.equals(mutatieDatumTot, that.mutatieDatumTot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(standTechnischeDatum, mutatieDatumVanaf, mutatieDatumTot);
        }

        @Override
        public String toString() {
            return "BagInfo{" +
                    "standTechnischeDatum=" + standTechnischeDatum +
                    ", mutatieDatumVanaf=" + mutatieDatumVanaf +
                    ", mutatieDatumTot=" + mutatieDatumTot +
                    '}';
        }
    }

    public BAG2GMLMutatieGroepStream(InputStream in) throws XMLStreamException {
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

        if(root.equals(BAG_STAND)) {
            isMutaties = false;
        } else if (root.equals(BAG_MUTATIES)) {
            isMutaties = true;
        } else {
            throw new IllegalArgumentException("XML root element moet bagStand of bagMutaties zijn");
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date standTechnischeDatum = null;
        if(isMutaties) {
            cursor = cursor.childElementCursor().advance();
            cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_BAG_EXTRACT_MUTATIES, "bagInfo");

            SMInputCursor bagInfoCursor = cursor.descendantElementCursor().advance();
            Date mutatieDatumVanaf = null, mutatieDatumTot = null;
            do {
                try {
                    switch (bagInfoCursor.getLocalName()) {
                        case "StandTechnischeDatum":
                            standTechnischeDatum = df.parse(bagInfoCursor.collectDescendantText());
                            break;
                        case "MutatiedatumVanaf":
                            mutatieDatumVanaf = df.parse(bagInfoCursor.collectDescendantText());
                            break;
                        case "MutatiedatumTot":
                            mutatieDatumTot = df.parse(bagInfoCursor.collectDescendantText());
                            break;
                    }
                } catch(ParseException ignored) {
                }
            } while(bagInfoCursor.getNext() != null);
            bagInfo = new BagInfo(standTechnischeDatum, mutatieDatumVanaf, mutatieDatumTot);

            cursor.getNext();
            cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_MUTATIELEVERING, "mutatieBericht");

            cursor = cursor.childElementCursor(new QName(NS_MUTATIELEVERING, "mutatieGroep"));

            hasMutatieGroep = cursor.getNext() != null;
        } else {
            cursor = cursor.childElementCursor().advance();
            cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_BAG_EXTRACT, "bagInfo");

            SMInputCursor bagInfoCursor = cursor.descendantElementCursor().advance();
            String gemeenteIdentificatie = null;
            do {
                switch(bagInfoCursor.getLocalName()) {
                    case "StandTechnischeDatum":
                        try {
                            standTechnischeDatum = df.parse(bagInfoCursor.collectDescendantText());
                        } catch(ParseException ignored) {
                        }
                        break;
                    case "GemeenteIdentificatie":
                        if (gemeenteIdentificatie != null) {
                            throw new IllegalArgumentException("Alleen een enkele GemeenteIdentificatie in een GemeenteCollectie wordt ondersteund");
                        }
                        gemeenteIdentificatie = bagInfoCursor.collectDescendantText();
                        break;
                    case "Gebied-NLD":
                        // "9999" is code for entire NL area
                        gemeenteIdentificatie = "9999";
                        break;
                }
            } while(bagInfoCursor.getNext() != null);

            bagInfo = new BagInfo(standTechnischeDatum, null, null, gemeenteIdentificatie);

            cursor.getNext();
            cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_STANDLEVERING, "standBestand");

            cursor = cursor.childElementCursor(new QName(NS_STANDLEVERING, "stand"));
        }

        return cursor;
    }

    public BagInfo getBagInfo() {
        return bagInfo;
    }

    @Override
    public Iterator<BAG2MutatieGroep> iterator() {
        return new Iterator<>() {
            SMEvent event = cursor.getCurrEvent();

            @Override
            public boolean hasNext() {
                if (isMutaties && !hasMutatieGroep) {
                    return false;
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
            public BAG2MutatieGroep next() {
                if (event == null) {
                    if(!hasNext()) {
                        throw new IllegalStateException("No more items");
                    }
                }
                // Make sure cursor.getNext() is called in a future next() call
                event = null;

                try {
                    if (isMutaties) {
                        if (!hasMutatieGroep) {
                            throw new IllegalStateException("No items");
                        }

                        cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_MUTATIELEVERING, "mutatieGroep");
                        SMInputCursor mutatieCursor = cursor.childElementCursor().advance();
                        List<BAG2Mutatie> mutaties = new ArrayList<>();
                        do {
                            String mutatieNaam = mutatieCursor.getLocalName();

                            Location location = mutatieCursor.getCursorLocation();
                            if (mutatieNaam.equals("wijziging")) {
                                SMInputCursor wijziging = mutatieCursor.childElementCursor().advance();
                                wijziging.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_MUTATIELEVERING, "was");
                                BAG2Object was = parseBAG2ObjectFromBagObjectParentElement(wijziging, NS_BAG_EXTRACT_MUTATIES);
                                wijziging.getNext();
                                wijziging.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_MUTATIELEVERING, "wordt");
                                BAG2Object wordt = parseBAG2ObjectFromBagObjectParentElement(wijziging, NS_BAG_EXTRACT_MUTATIES);

                                mutaties.add(new BAG2WijzigingMutatie(location, was, wordt));
                            } else if (mutatieNaam.equals("toevoeging")) {
                                SMInputCursor wijziging = mutatieCursor.childElementCursor().advance();
                                wijziging.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_MUTATIELEVERING, "wordt");
                                BAG2Object toevoeging = parseBAG2ObjectFromBagObjectParentElement(wijziging, NS_BAG_EXTRACT_MUTATIES);
                                mutaties.add(new BAG2ToevoegingMutatie(location, toevoeging));

                            } else if (mutatieNaam.equals("verwijdering")) {
                                throw new IllegalArgumentException("Verwijdering-mutaties mogen niet voorkomen in de BAG2");
                            } else {
                                throw new IllegalArgumentException("Onbekende mutatie: " + mutatieNaam);
                            }
                        } while(mutatieCursor.getNext() == SMEvent.START_ELEMENT);

                        return new BAG2MutatieGroep(mutaties);
                    } else {
                        cursor.getStreamReader().require(XMLStreamConstants.START_ELEMENT, NS_STANDLEVERING, "stand");
                        Location location = cursor.getCursorLocation();
                        BAG2Object object = parseBAG2ObjectFromBagObjectParentElement(cursor, NS_BAG_EXTRACT);
                        return new BAG2MutatieGroep(List.of(new BAG2ToevoegingMutatie(location, object)));
                    }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private BAG2Object parseBAG2ObjectFromBagObjectParentElement(SMInputCursor bagObjectParentCursor, String bagObjectNamespace) throws XMLStreamException, FactoryException, IOException {
        SMInputCursor bagObjectCursor = bagObjectParentCursor
                .childElementCursor(new QName(bagObjectNamespace, "bagObject")).advance()
                .childElementCursor().advance();
        return parseBAG2Object(bagObjectCursor);
    }

    private BAG2Object parseBAG2Object(SMInputCursor bagObjectCursor) throws XMLStreamException, FactoryException, IOException {
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
    }

    private void parseAttribute(SMInputCursor attribute, Map<String, Object> objectAttributes) throws XMLStreamException, FactoryException, IOException {
        String attributeName = attribute.getLocalName();

        switch (attributeName) {
            case "geometrie":
                // Position cursor at child element
                SMInputCursor geomCursor = attribute.childElementCursor().advance();
                // Sometimes there is an element like "punt" which has the actual geometry as child element
                if (!geomCursor.getNsUri().equals(NS_GML_32)) {
                    geomCursor.childElementCursor().advance();
                }
                Geometry geom = geometryReader.readGeometry();
                geom.setSRID(SRID);
                objectAttributes.put(attributeName, geom);
                break;
            case "voorkomen":
                // Flatten al Voorkomen child attributes, according to the schema the element names do not conflict
                parseVoorkomen(attribute, objectAttributes);
                break;
            case "BeschikbaarLV":
                // Flatten al Voorkomen/BeschikbaarLV child attributes to the top level, according to the schema the element
                // names do not conflict
                parseBeschikbaarLV(attribute, objectAttributes);
                break;
            case "heeftAlsNevenadres":
                parseNevenadres(attribute, objectAttributes);
                break;
            case "maaktDeelUitVan":
                parseMaaktDeelUitVan(attribute, objectAttributes);
                break;
            case "gebruiksdoel":
                parseGebruiksdoel(attribute, objectAttributes);
                break;
            default:
                // String attribute value as default

                // This also works for ligtIn en ligtAan
                objectAttributes.put(attributeName, attribute.collectDescendantText().trim());
                break;
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
