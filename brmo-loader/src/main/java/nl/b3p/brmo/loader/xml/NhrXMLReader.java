package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.NhrBericht;
import nl.b3p.brmo.loader.entity.NhrBerichten;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Deze reader splitst een nHR soap response in berichten.
 *
 * @author Matthijs Laan
 * @author mprins
 */
public class NhrXMLReader extends BrmoXMLReader {

    public static final String PREFIX_BSN = "nhr.bsn.natPers.";
    public static final String PREFIX_NAAM = "nhr.naam.natPers.";

    private static final Log LOG = LogFactory.getLog(NhrXMLReader.class);

    private static Templates splitTemplates;

    private static Transformer t;

    private Iterator<NhrBericht> iterator;

    private int volgorde = 0;

    private String brOrigXML = null;

    public NhrXMLReader(InputStream in) throws Exception {
        initTemplates();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        in = new TeeInputStream(in, bos, true);

        // Split input naar multiple berichten
        DOMResult r = new DOMResult();
        splitTemplates.newTransformer().transform(new StreamSource(in), r);

        JAXBContext jc = JAXBContext.newInstance(NhrBerichten.class, NhrBericht.class, Bericht.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        NhrBerichten b = (NhrBerichten)unmarshaller.unmarshal(new DOMSource(r.getNode()));

        if(b == null || b.berichten == null) {
            throw new BrmoLeegBestandException("Geen BRMO berichten gevonden in NHR XML");
        }

        if(!b.berichten.isEmpty()) {
            setBestandsDatum(b.berichten.get(0).getDatum());
        }

        iterator = b.berichten.iterator();

        brOrigXML = bos.toString(StandardCharsets.UTF_8.name());
        LOG.debug("Originele nHR xml is: \n" + brOrigXML);

        init();
    }

    private synchronized void initTemplates() throws Exception {
        if(splitTemplates == null) {
            LOG.info("Initializing NHR split XSL templates...");
            Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/nhr-split-3.0.xsl"));
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    return new StreamSource(NhrXMLReader.class.getResourceAsStream("/xsl/" + href));
                }
            });
            NhrXMLReader.splitTemplates = tf.newTemplates(xsl);

            t = TransformerFactory.newInstance().newTransformer();
        }
    }

    @Override
    public void init() throws Exception {
        soort = BrmoFramework.BR_NHR;
    }

    @Override
    public boolean hasNext() throws Exception {
        return iterator.hasNext();
    }

    @Override
    public NhrBericht next() throws Exception {
        NhrBericht b = iterator.next();

        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(b.getNode().getFirstChild()), new StreamResult(sw));
        // opzoeklijst van bsn en/of volledige naam en hash toevoegen
        StringBuilder xml = new StringBuilder(sw.toString());
        String bsns = getXML(extractBSN(xml.toString()));
        LOG.trace("gevonden bsn en naam sleutels: "+ bsns);
        // insert bsnhashes voor de laatste node
        xml.insert(xml.lastIndexOf("</"), bsns);

        b.setBrXml(xml.toString());

        // we willen alleen bij het eerst geparsede nhr bericht de originele soap hebben
        if (volgorde == 0) {
            b.setBrOrgineelXml(brOrigXML);
        }

        b.setVolgordeNummer(volgorde++);
        return b;
    }

    /**
     * maakt een map met bsn,bsnhash.
     *
     * @param brXml string
     * @return hashmap met bsn/naamzonderspaties,bsnhash
     *
     */
    public Map<String, String> extractBSN(String brXml) {
        Map<String, String> bsnHashes = new HashMap<>();
//        LOG.trace("deel bericht xml: "+brXml);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(brXml)));

            NodeList nodeList = doc.getElementsByTagNameNS("*","bsn");
            for (int i = 0; i < nodeList.getLength(); i++) {
                LOG.debug("BSN: " + i+": "+nodeList.item(i).getNodeName() +" - "+ nodeList.item(i).getTextContent());
                bsnHashes.put(nodeList.item(i).getTextContent(), getHash(nodeList.item(i).getTextContent()));
            }

            nodeList = doc.getElementsByTagNameNS("*", "volledigeNaam");
            String _cleanName;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getParentNode().getLocalName().equals("natuurlijkPersoon")) {
                    LOG.debug("NAAM: " + i + ": " + nodeList.item(i).getNodeName() + " - " + nodeList.item(i).getTextContent());
                    _cleanName = nodeList.item(i).getTextContent()
                            .replace(" ", "")
                            .replace("'", "");
                    bsnHashes.put(_cleanName, getHash(_cleanName));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Fout tijdens toevoegen bsn hashes", e);
        }
        return bsnHashes;
    }

    public String getXML(Map<String, String> map) {
        StringBuilder root = new StringBuilder();
        if (!map.isEmpty()) {
            root.append("<cat:bsnhashes xmlns:cat=\"http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02\">");

            String type;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                    root.append("<cat:");
                    if (NumberUtils.isCreatable(entry.getKey())){
                        type = PREFIX_BSN;
                    } else {
                        type = PREFIX_NAAM;
                    }
                    root.append(type).append(entry.getKey()).append(">")
                            .append(entry.getValue())
                            .append("</cat:").append(type).append(entry.getKey()).append(">");
                }
            }
            root.append("</cat:bsnhashes>");
        }
        return root.toString();
    }
}
