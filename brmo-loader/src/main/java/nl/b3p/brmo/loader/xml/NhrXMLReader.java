package nl.b3p.brmo.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.NhrBericht;
import nl.b3p.brmo.loader.entity.NhrBerichten;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Matthijs Laan
 */
public class NhrXMLReader extends BrmoXMLReader {

    private static final Log LOG = LogFactory.getLog(NhrXMLReader.class);

    private static Templates splitTemplates;

    private static Transformer t;

    Iterator<NhrBericht> iterator;
    int volgorde = 0;

    public static final String PREFIX = "nhr.bsn.natPers.";

    public NhrXMLReader(InputStream in) throws Exception {
        initTemplates();

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
        // opzoeklijst van bsn en hash toevoegen
        StringBuilder xml = new StringBuilder(sw.toString());
        String bsns = getXML(extractBSN(xml.toString()));
        // insert bsnhashes voor de laatste node
        xml.insert(xml.lastIndexOf("</"), bsns);

        b.setBrXml(xml.toString());
        b.setVolgordeNummer(volgorde++);
        return b;
    }

    /**
     * maakt een map met bsn,bsnhash.
     *
     * @param brXml string
     * @return hashmap met bsn,bsnhash
     *
     */
    public Map<String, String> extractBSN(String brXml) {
        Map<String, String> bsnHashes = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(brXml)));
            NodeList nodeList = doc.getElementsByTagName("cat:bsn");
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                LOG.debug(nodeList.item(i).getTextContent());
                bsnHashes.put(nodeList.item(i).getTextContent(), getHash(nodeList.item(i).getTextContent()));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Fout tijdens toevoegen bsn hashes", e);
        }
        return bsnHashes;
    }

    public String getXML(Map<String, String> map) throws ParserConfigurationException {
        StringBuilder root = new StringBuilder();
        if (!map.isEmpty()) {
            root.append("<cat:bsnhashes>");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                    root.append("<cat:").append(PREFIX).append(entry.getKey()).append(">")
                            .append(entry.getValue())
                            .append("</cat:").append(PREFIX).append(entry.getKey()).append(">");
                }
            }
            root.append("</cat:bsnhashes>");
        }
        return root.toString();
    }
}
