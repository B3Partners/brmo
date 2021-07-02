package nl.b3p.brmo.imgeo;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMHierarchicCursor;
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
import java.io.FileInputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.imgeo.IMGeoSchema.fixUUID;

public class IMGeoObjectStreamer implements Iterable<IMGeoObject> {
    private static final String NS_IMGEO = "http://www.geostandaarden.nl/imgeo/2.1";
    private static final String NS_CITYGML = "http://www.opengis.net/citygml/2.0";
    private static final String NS_GML = "http://www.opengis.net/gml";
    private static final String NS_MUTATIELEVERING = "http://www.kadaster.nl/schemas/mutatielevering-generiek/2.0";
    private static final String NS_MUTATIELEVERING_BGT = "http://www.kadaster.nl/schemas/mutatielevering-bgt/1.0";

    private static final QName CITYGML_CITY_OBJECT_MEMBER = new QName(NS_CITYGML, "cityObjectMember");

    private static final int SRID = 28992;

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cursor;

    private boolean isMutaties;

    public IMGeoObjectStreamer(File f) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(f));
        this.geometryReader = buildGeometryReader();
    }

    public IMGeoObjectStreamer(InputStream in) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(in));
        this.geometryReader = buildGeometryReader();
    }

    public IMGeoObjectStreamer(Reader r) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(r));
        this.geometryReader = buildGeometryReader();
    }

    private SMInputCursor initCursor(SMInputCursor cursor) throws XMLStreamException {
        QName root = cursor.advance().getQName();

        if (root.equals(new QName(NS_CITYGML, "CityModel"))) {
            isMutaties = false;
        } else if (root.equals(new QName(NS_MUTATIELEVERING_BGT, "bgtMutaties"))) {
            isMutaties = true;
        } else {
            throw new IllegalArgumentException("XML root element moet CityModel of bgtMutaties zijn");
        }

        if(isMutaties) {
            cursor = cursor.childElementCursor(new QName(NS_MUTATIELEVERING, "mutatieBericht")).advance().childElementCursor().advance();
            do {
                if (cursor.getLocalName().equals("dataset")) {
                    assert cursor.getText().equals("bgt");
                } else if (cursor.getLocalName().equals("inhoud")) {
                    // TODO parse inhoud
                } else if(cursor.getLocalName().equals("mutatieGroep")) {
                    // cursor positioned correctly
                    break;
                } else {
                    throw new IllegalStateException("Verwacht mutatieGroep element, gevonden " + cursor.getQName());
                }
                cursor.getNext();
            } while(true);

        } else {
            cursor = cursor.childElementCursor(new QName(NS_CITYGML, "cityObjectMember")).advance();
        }

        return cursor;
    }

    protected XmlStreamGeometryReader buildGeometryReader() {
        return new XmlStreamGeometryReader(this.cursor.getStreamReader());
    }

    private SMInputCursor getCityObjectMemberCursor(SMHierarchicCursor root) throws XMLStreamException {
        return root.advance().childElementCursor(CITYGML_CITY_OBJECT_MEMBER);
    }

    protected SMInputFactory buildSMInputFactory() {
        // Supporting StAX2 API:
        //final XMLInputFactory stax = new WstxInputFactory(); // Woodstox
        //final XMLInputFactory stax = new com.fasterxml.aalto.stax.InputFactoryImpl(); // Aalto
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory(); // JRE Default, depends on JAR's present or javax.xml.stream.XMLInputFactory property, can be SJSXP

        //System.out.println("Default JAXP StAX XMLInputFactory: " + XMLInputFactory.newFactory().getClass());
        //System.out.println("Used StAX XMLInputFactory: " + xmlInputFactory.getClass());

        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE); // Coalesce characters
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE); // No XML entity expansions or external entities

        return new SMInputFactory(xmlInputFactory);
    }

    // TODO: skip features for resume support

    @Override
    public Iterator<IMGeoObject> iterator() {
        return new Iterator<IMGeoObject>() {
            SMEvent event = cursor.getCurrEvent();

            /**
             * A parse action may return multiple objects. Buffer the future objects to be returned in a next() call.
             */
            private final Queue<IMGeoObject> buffer = new LinkedList<>();

            @Override
            public boolean hasNext() {
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
            public IMGeoObject next() {
                try {
                    if (!buffer.isEmpty()) {
                        return buffer.remove();
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
                    IMGeoObject.MutatieStatus mutatieStatus = IMGeoObject.MutatieStatus.WORDT;

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
                            mutatieStatus = IMGeoObject.MutatieStatus.WAS_WORDT;
                        } else if(mutatieNaam.equals("toevoeging")) {
                            assert mutatie.getLocalName().equals("wordt");
                            // mutatieStatus is already IMGeoObject.MutatieStatus.WORDT
                        } else {
                            throw new IllegalStateException("Ongeldig mutatie element: " + mutatie.getLocalName());
                        }

                        // Move cursor from <wordt> to <cityObjectMember> child
                        cityObjectMemberChild = mutatie.childElementCursor(new QName(NS_MUTATIELEVERING_BGT, "bgtObject")).advance()
                                .childElementCursor(new QName(NS_CITYGML, "cityObjectMember")).advance()
                                .childElementCursor().advance();
                    } else {
                        cityObjectMemberChild = cursor.childElementCursor().advance();
                    }

                    final String name = cityObjectMemberChild.getLocalName();
                    final Location location = cityObjectMemberChild.getCursorLocation();
                    attributes.put("gmlId", fixUUID(cityObjectMemberChild.getAttrValue(NS_GML, "id")));

                    SMInputCursor attributesCursor = cityObjectMemberChild.childElementCursor();

                    // Only a single collection attribute is supported (no matrix of multiple collection attributes)
                    // An IMGeoObject will be returned for each collection item
                    Collection<Map<String,Object>> collectionAttribute = null;

                    while (attributesCursor.getNext() != null) {
                        String attributeName = attributesCursor.getLocalName();
                        Object parsedAttribute = parseIMGeoAttribute(attributesCursor);

                        if (parsedAttribute instanceof IMGeoObject) {
                            List<IMGeoObject> oneToMany = (List<IMGeoObject>)attributes.get(attributeName);
                            if (oneToMany == null) {
                                oneToMany = new ArrayList<>();
                                attributes.put(attributeName, oneToMany);
                            }
                            oneToMany.add((IMGeoObject) parsedAttribute);
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
                             attributesForCollectionItem.put(IMGeoSchema.INDEX, index++);
                             buffer.add(new IMGeoObject(name, attributesForCollectionItem, location));
                        }
                        return buffer.remove();
                    } else {
                        return new IMGeoObject(name, attributes, location, mutatieStatus, gmlIdPreviousVersion);
                    }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    public static void main(String[] args) throws Exception {
        //Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");

//        String dir = "/home/matthijsln/dev/brmo/work/bgt-citygml-loader/src/test/resources/";
        //String file = "bgt_weginrichtingselement_single.gml";
        //String file = "bgt_tunneldeel_single.gml";
//        String file = "bgt_pand_multiple.gml";

        ZipFile file = new ZipFile(new File("/media/ssd/files/bgt/2021/bgt-citygml-nl-nopbp.zip"));
        ZipEntry entry = file.getEntry("bgt_openbareruimtelabel.gml");

        int objects = 0;
        boolean log = true;
        long startTime = System.currentTimeMillis();

        try(InputStream input = /*file.getInputStream(entry)*/new FileInputStream("//media/ssd/files/bgt/2021/bgt_pand.xml")) {
//        try(InputStream input = new FileInputStream(dir + file)) {
            IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(input);
            Map<Integer,Integer> counts = new HashMap<>();

            for (IMGeoObject object: streamer) {
                objects++;

                if(log) System.out.printf("cityObjectMember #%d: %s%s %s\n",
                        objects,
                        object.getMutatieStatus(),
                        object.getMutatieStatus() == IMGeoObject.MutatieStatus.WAS_WORDT ? " (previous gmlId " + object.getMutatiePreviousVersionGmlId() + ")" : "",
                        object);

                if (objects % 1000000 == 0) {
                    System.out.printf("Parsed %d objects\n", objects);
                }
                if (objects == 400) {
                    break;
                }
            }
            System.out.println("Size counts: " + counts);
        }
        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.printf("Finished streaming: %d objects, %.1f s, %.2f objects/s\n", objects, time, objects / time);
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
            String id = attribute.descendantElementCursor(new QName(NS_IMGEO, "lokaalID")).advance().collectDescendantText().trim();
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
            return new IMGeoObject("nummeraanduidingreeks", nummeraanduidingreeks, location);
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

    private Map<String,Object> parseNummeraanduidingreeks(SMInputCursor cursor) throws XMLStreamException, FactoryException, IOException {
        Map<String,Object> label = new HashMap<>();

        cursor = cursor.childElementCursor(new QName(NS_IMGEO, "Nummeraanduidingreeks")).advance().childElementCursor();
        while (cursor.getNext() != null) {
            if (cursor.getQName().equals(new QName(NS_IMGEO, "nummeraanduidingreeks"))) {
                SMInputCursor labelChilds = cursor.childElementCursor(new QName(NS_IMGEO, "Label")).advance().childElementCursor();
                while(labelChilds.getNext() != null) {
                    if (labelChilds.hasLocalName("tekst")) {
                        label.put("tekst", labelChilds.collectDescendantText().trim());
                    } else if (labelChilds.hasLocalName("positie")) {
                        SMInputCursor labelPositie = labelChilds.childElementCursor(new QName(NS_IMGEO, "Labelpositie")).advance();

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

        cursor = cursor.childElementCursor(new QName(NS_IMGEO, "Label")).advance().childElementCursor();

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
