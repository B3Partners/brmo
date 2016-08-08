/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.BagBericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * reader of BAG mutatie files
 *
 * @author Boy de Wit
 * @author mprins
 */
public class BagXMLReader extends BrmoXMLReader {

    private static final Log log = LogFactory.getLog(BagXMLReader.class);

    private final XMLInputFactory factory = XMLInputFactory.newInstance();
    private final XMLStreamReader streamReader;
    private final Transformer transformer;
    private final DocumentBuilder builder;
    private final XMLOutputFactory xmlof;

    public static final String MUTATIE_PRODUCT = "Mutatie-product";
    public static final String LVC_PRODUCT = "LVC-product";
    private static final String DATUM_TIJD_LV = "datumtijdstempelLV";
    private static final String MUTATIE_DATUMTOT = "MutatiedatumTot";
    private static final String STAND_PEILDATUM = "StandPeildatum";
    private static final String STAND_TECHNISCHEDATUM = "StandTechnischeDatum";

    private BagBericht nextBericht = null;

    public static final Map<String,String> lvcProductToObjectType;

    static {
        Map<String,String> m = new HashMap();
        m.put("Ligplaats", "LIG");
        m.put("Nummeraanduiding", "NUM");
        m.put("OpenbareRuimte", "OPR");
        m.put("Pand", "PND");
        m.put("Verblijfsobject", "VBO");
        m.put("Standplaats", "STA");
        m.put("Woonplaats", "WPL");
        lvcProductToObjectType = Collections.unmodifiableMap(m);
    }

    public BagXMLReader(InputStream in)
            throws XMLStreamException,
            TransformerConfigurationException,
            ParserConfigurationException {

        streamReader = factory.createXMLStreamReader(in);

        TransformerFactory tf = TransformerFactory.newInstance();
        transformer = tf.newTransformer();

        // Vanwege splitsing is repairen van namespaces nodig, anders ontbreekt
        // de namespace prefix declaratie xmlns:xlink="http://www.w3.org/1999/xlink"
        // in de output en mislukt de XSL transformatie
        xmlof = XMLOutputFactory.newInstance();
        xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
        dbfactory.setNamespaceAware(true);
        dbfactory.setNamespaceAware(true);
        builder = dbfactory.newDocumentBuilder();

        init();
    }

    @Override
    public void init() {
        soort = BrmoFramework.BR_BAG;

        String technischeDatum = null;
        String peilDatum = null;
        String mutatieDatumTot = null;
        boolean firstStartElement = true;
        try {
            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String localName = streamReader.getLocalName();

                    if (localName.equals(MUTATIE_PRODUCT) || localName.equals(LVC_PRODUCT)) {
                        break;
                    }
                    //tbv bag-fragment met enkel object zonder wrapper
                    if (lvcProductToObjectType.containsKey(localName) 
                            && firstStartElement) {
                        break;
                    }

                    if (localName.equals(DATUM_TIJD_LV)) {
                        setDatumAsString(streamReader.getElementText());
                    }
                    if(localName.equals(STAND_TECHNISCHEDATUM)) {
                        technischeDatum = streamReader.getElementText();
                    }
                    if(localName.equals(STAND_PEILDATUM)) {
                        peilDatum = streamReader.getElementText();
                    }
                    if(localName.equals(MUTATIE_DATUMTOT)) {
                        mutatieDatumTot = streamReader.getElementText();
                    }
                    
                    firstStartElement = false;
                }
                streamReader.next();
            }
        } catch (XMLStreamException ex) {
            log.error("Error while streaming XML", ex);
        }
        if(peilDatum != null) {
            setDatumAsString(peilDatum, "yyyyMMdd");
        } else if(technischeDatum != null) {
            setDatumAsString(technischeDatum, "yyyyMMdd");
        } else if(mutatieDatumTot != null) {
            setDatumAsString(mutatieDatumTot, "yyyy-MM-dd");
        } else {
            setBestandsDatum(new Date());
        }
    }

    @Override
    public boolean hasNext() throws SAXException, IOException, TransformerException {
        if (nextBericht != null) {
            return true;
        }
        try {
            while (streamReader.hasNext()) {
                if (streamReader.isStartElement() && streamReader.getLocalName().equals(MUTATIE_PRODUCT)) {
                    // parse en check voor "Nieuw" element en niet "Origineel"/"Wijziging"
                    StringWriter sw = new StringWriter();

                    // Hiermee wordt de StreamReader het volgende product geforward
                    transformer.transform(new StAXSource(streamReader), new StAXResult(xmlof.createXMLStreamWriter(sw)));

                    Document d = builder.parse(new InputSource(new StringReader(sw.toString())));
                    if (createMutatieBagBericht(sw.toString(), d)) {
                        return true;
                    }
                 } else if(streamReader.isStartElement() && lvcProductToObjectType.containsKey(streamReader.getLocalName())) {
                    StringWriter sw = new StringWriter();
                    // Hiermee wordt de StreamReader het volgende product geforward
                    transformer.transform(new StAXSource(streamReader), new StAXResult(xmlof.createXMLStreamWriter(sw)));
                    Document d = builder.parse(new InputSource(new StringReader(sw.toString())));
                    if (createStandBagBericht(sw.toString(), d)) {
                        return true;
                    }
                } else {
                    streamReader.next();
                }
            }
        } catch (XMLStreamException ex) {
            log.error("Error while streaming XML", ex);
        }
        return false;
    }

    @Override
    public BagBericht next() throws TransformerException, XMLStreamException {
        BagBericht b = nextBericht;
        nextBericht = null;
        return b;
    }
    
    private boolean createStandBagBericht(String brXml, Document d) {
        if (gebruikStandBericht(d.getDocumentElement())) {
            nextBericht = new BagBericht(brXml, d);
            Date datBegGeld = getBerichtBeginGeldigheid(d.getDocumentElement());
            if (datBegGeld != null && getBestandsDatum().before(datBegGeld)) {
                //uitzondering: begindatum in toekomst
                //sla op als mutatie met datum in de toekomst
                //als dit 2x voorkomt voor zelfde object, jammer dan!
                nextBericht.setDatum(datBegGeld);
                nextBericht.setVolgordeNummer(0);
            } else {
                // Bij een standlevering zit er geen datum in een "Verwerking" element,
                // pak peildatum van stand en zet volgordenr op -1
                nextBericht.setDatum(getBestandsDatum());
                nextBericht.setVolgordeNummer(-1);
            }
            return true;
        }
        return false;
    }
   
    private boolean createMutatieBagBericht(String brXml, Document d) {
        NodeList children = d.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            // Mutatie-product met Nieuw als child element gevonden
            // Origineel/Wijziging kunnen overgeslagen worden omdat er altijd een Nieuw
            // element voor hetzelfde object is. In de BRMO wordt de historie zelf
            // bijgehouden dus zijn de wijzigingen niet interessant
            if ("Nieuw".equals(child.getLocalName())) {
                NodeList grandchildren = child.getChildNodes();
                for (int j = 0; j < grandchildren.getLength(); j++) {
                    Node grandchild = grandchildren.item(j);
                    if (grandchild.getNodeType() == Node.ELEMENT_NODE) {
                        nextBericht = new BagBericht(brXml, d);
                        Date datBegGeld = getBerichtBeginGeldigheid(grandchild);
                        if (datBegGeld != null && getBestandsDatum().before(datBegGeld)) {
                            //uitzondering: begindatum in toekomst
                            //sla op met datum in de toekomst
                            nextBericht.setDatum(datBegGeld);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @param n bericht
     * @return true als record niet inactief en einddatum geldigheid na bestandsdatum
     */
    private boolean gebruikStandBericht(Node n) {
        String soort = n.getLocalName();
        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("tijdvakgeldigheid".equals(child.getLocalName())) {
                NodeList grandchildren = child.getChildNodes();
                for (int j = 0; j < grandchildren.getLength(); j++) {
                    Node grandchild = grandchildren.item(j);
                    if ("einddatumTijdvakGeldigheid".equals(grandchild.getLocalName())) {
                        String dateString = grandchild.getTextContent().substring(0, 8);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        Date eindGeldigheidDatum = null;
                        try {
                            eindGeldigheidDatum = sdf.parse(dateString);
                        } catch (ParseException pe) {
                            log.error("Fout bij parsen datum \"" + dateString + "\" met formaat yyyyMMdd", pe);
                        }
                        if (eindGeldigheidDatum != null 
                                && getBestandsDatum().after(eindGeldigheidDatum)) {
                            return false;
                        }
                    }
                }
            }
            // aanduidingRecordInactief J verwijderen
            if ("aanduidingRecordInactief".equals(child.getLocalName())) {
                if (child.getTextContent().equalsIgnoreCase("J")) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * @param n bericht
     * @return datum begin geldigheid of null indien niet in toekomst
     */
    private Date getBerichtBeginGeldigheid(Node n) {
        NodeList children = n.getChildNodes();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date beginGeldigheid = null;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("tijdvakgeldigheid".equals(child.getLocalName())) {
                NodeList grandchildren = child.getChildNodes();
                for (int j = 0; j < grandchildren.getLength(); j++) {
                    Node grandchild = grandchildren.item(j);
                    if ("begindatumTijdvakGeldigheid".equals(grandchild.getLocalName())) {
                        String dateString = grandchild.getTextContent().substring(0, 8);
                        try {
                            beginGeldigheid = sdf.parse(dateString);
                        } catch (ParseException pe) {
                            log.error("Fout bij parsen datum \"" + dateString + "\" met formaat yyyyMMdd", pe);
                        }
                    }
                 }
            }
        }
        return beginGeldigheid;
    }
}
