package nl.b3p.brmo.loader.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Boy de Wit
 */
public class RsgbTransformer {
    private static final Log log = LogFactory.getLog(RsgbTransformer.class);
    private final Templates t;
    private final DocumentBuilder db;

    public RsgbTransformer(String pathToXsl)
            throws TransformerConfigurationException, ParserConfigurationException {

        Split init = SimonManager.getStopwatch("b3p.rsgb.transformer.init").start();

        Source xsl = new StreamSource(this.getClass().getResourceAsStream(pathToXsl));
        TransformerFactory tf = TransformerFactory.newInstance();
        this.t = tf.newTemplates(xsl);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.db = dbf.newDocumentBuilder();
        init.stop();
    }

    public String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        Split transform = SimonManager.getStopwatch("b3p.rsgb.transformer.transform").start();

        StringWriter sw = new StringWriter();
        Document d = db.parse( new InputSource(new StringReader(bericht.getBrXml())));
        t.newTransformer().transform(new DOMSource(d), new StreamResult(sw));

        transform.stop();
        return sw.toString();
    }
}
