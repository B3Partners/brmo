package nl.b3p.brmo.imgeo;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.gml.stream.XmlStreamGeometryReader;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.locationtech.jts.geom.Geometry;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IMGeoObjectStreamer implements Iterable<IMGeoObject> {
    private static final String NS_IMGEO = "http://www.geostandaarden.nl/imgeo/2.1";
    private static final String NS_CITYGML = "http://www.opengis.net/citygml/2.0";
    private static final String NS_GML = "http://www.opengis.net/gml";

    private static final QName CITYGML_CITY_OBJECT_MEMBER = new QName(NS_CITYGML, "cityObjectMember");

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cityObjectMembers;

    public IMGeoObjectStreamer(File f) throws XMLStreamException {
        this.cityObjectMembers = getCityObjectMemberCursor(buildSMInputFactory().rootElementCursor(f));
        this.geometryReader = buildGeometryReader();
    }

    public IMGeoObjectStreamer(InputStream in) throws XMLStreamException {
        this.cityObjectMembers = getCityObjectMemberCursor(buildSMInputFactory().rootElementCursor(in));
        this.geometryReader = buildGeometryReader();
    }

    public IMGeoObjectStreamer(Reader r) throws XMLStreamException {
        this.cityObjectMembers = getCityObjectMemberCursor(buildSMInputFactory().rootElementCursor(r));
        this.geometryReader = buildGeometryReader();
    }

    protected XmlStreamGeometryReader buildGeometryReader() {
        return new XmlStreamGeometryReader(this.cityObjectMembers.getStreamReader());
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
            SMEvent event = null;
            @Override
            public boolean hasNext() {
                if (event != null) {
                    return true;
                }
                try {
                    event = cityObjectMembers.getNext();
                    return event != null;
                } catch(XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public IMGeoObject next() {
                try {
                    if (event == null) {
                        if(!hasNext()) {
                            throw new IllegalStateException("No more items");
                        }
                    }
                    // Make sure cityObjectMembers.getNext() is called with next Iterator.getNext() call
                    event = null;

                    Map<String, Object> attributes = new HashMap<>();

                    SMInputCursor cityObjectMemberChild = cityObjectMembers.childElementCursor().advance();

                    final String name = cityObjectMemberChild.getLocalName();
                    final Location location = cityObjectMemberChild.getCursorLocation();
                    attributes.put("gmlId", cityObjectMemberChild.getAttrValue(NS_GML, "id"));

                    SMInputCursor attributesCursor = cityObjectMemberChild.childElementCursor();

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
                        } else {
                            attributes.put(attributeName, parsedAttribute);
                        }
                    }
                    return new IMGeoObject(name, attributes, location);
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

        try(InputStream input = file.getInputStream(entry)) {
//        try(InputStream input = new FileInputStream(dir + file)) {
            IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(input);
            Map<Integer,Integer> counts = new HashMap<>();

            for (IMGeoObject object: streamer) {
                objects++;

                List openbareRuimteNaam = (List)object.getAttributes().get("openbareRuimteNaam");
                int size = openbareRuimteNaam == null ? 0 : openbareRuimteNaam.size();
                Integer count = counts.get(size);
                count = count == null ? 1 : count +1;
                counts.put(size, count);
                if(openbareRuimteNaam != null && openbareRuimteNaam.size() > 2) System.out.printf("cityObjectMember #%d: %s\n",
                        objects,
                        openbareRuimteNaam);

                if (objects % 1000000 == 0) {
                    System.out.printf("Parsed %d objects\n", objects);
                }
                if (objects == 400000) {
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
            CoordinateReferenceSystem crs = (CoordinateReferenceSystem)geom.getUserData();
            Identifier id = AbstractIdentifiedObject.getIdentifier(crs, Citations.EPSG);
            if (id != null) {
                String code = id.getCode();
                geom.setSRID(Integer.parseInt(code));
            }
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
                        positie.put("plaatsingspunt", geometryReader.readGeometry());
                    } else {
                        positie.put(positieCursor.getLocalName(), positieCursor.collectDescendantText().trim());
                    }
                }
            }
        }

        return posities;
    }
}
