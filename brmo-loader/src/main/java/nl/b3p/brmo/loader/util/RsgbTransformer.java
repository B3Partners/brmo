package nl.b3p.brmo.loader.util;

import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Boy de Wit
 */
public class RsgbTransformer {
    private static final Log log = LogFactory.getLog(RsgbTransformer.class);
    protected final Templates t;
    protected final DocumentBuilder db;

    public RsgbTransformer(String pathToXsl)
            throws TransformerConfigurationException, ParserConfigurationException {

        Source xsl = new StreamSource(this.getClass().getResourceAsStream(pathToXsl));
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setURIResolver((href, base) -> new StreamSource(RsgbTransformer.class.getResourceAsStream("/xsl/" + href)));
        this.t = tf.newTemplates(xsl);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.db = dbf.newDocumentBuilder();
    }

    public String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerException {
        StringWriter sw = new StringWriter();
        Document d = db.parse(new InputSource(new StringReader(bericht.getBrXml())));
        Transformer transformer = t.newTransformer();
        transformer.setParameter("objectRef", bericht.getObjectRef() == null ? "" : bericht.getObjectRef());
        transformer.setParameter("datum", bericht.getDatum() == null ? "" : bericht.getDatum());
        transformer.setParameter("volgordeNummer", bericht.getVolgordeNummer() == null ? "" : bericht.getVolgordeNummer());
        transformer.setParameter("soort", bericht.getSoort() == null ? "" : bericht.getSoort());
        transformer.transform(new DOMSource(d), new StreamResult(sw));

        return sw.toString();
    }

    public Node transformToDbXmlNode(Bericht bericht) throws SAXException, IOException, TransformerException {
        Document d = db.parse(new InputSource(new StringReader(bericht.getBrXml())));
        DOMResult r = new DOMResult();
        t.newTransformer().transform(new DOMSource(d), r);
        return r.getNode();
    }
}
