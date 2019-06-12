package nl.b3p.brmo.loader;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author meine
 */
public class RsgbBRPTransformer extends RsgbTransformer {

    private static final Log log = LogFactory.getLog(RsgbBRPTransformer.class);
    private StagingProxy staging;

    public RsgbBRPTransformer(String pathToXsl, StagingProxy staging) throws TransformerConfigurationException, ParserConfigurationException {
        super(pathToXsl);
        this.staging = staging;
    }

    @Override
    public String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        String current = super.transformToDbXml(bericht);
        StringBuilder loadLog = new StringBuilder();

        try {
            Bericht old = staging.getOldBericht(bericht, loadLog);

            if (old != null) {
                Document d = merge( old.getDbXml(), current);
                String mergedDBXML = print(d);
                bericht.setDbXml(mergedDBXML);
                current = mergedDBXML;                
            }
        } catch (SQLException ex) {
            log.error("Cannot retrieve old bericht: ", ex);
        } catch (Exception ex) {
            log.error("Cannot retrieve old bericht: ", ex);
        }

        // retrieve old bericht
        // apply current to old
        // return modified dbxml
        return current;
    }

    @Override
    public Node transformToDbXmlNode(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        Node n = super.transformToDbXmlNode(bericht);

        // retrieve old bericht
        // apply current to old
        // return modified dbxml
        return n;
    }

    protected static Document merge(String oldFile, String newFile) throws Exception {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expression = xpath.compile("/root/data");
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document base = docBuilder.parse(new InputSource(new StringReader(oldFile)));

        Element old = (Element) expression.evaluate(base, XPathConstants.NODE);
        if (old == null) {
            throw new IOException(oldFile + ": expression does not evaluate to node");
        }

        Document merge = docBuilder.parse(new InputSource(new StringReader(newFile)));
        Node newNode = (Node) expression.evaluate(merge, XPathConstants.NODE);
        
        /*
            (1)Voor elke node in merge, kijk of hij bestaat in base
                zo nee, importeer
                zo ja, kijk of dit het een na diepste niveau is
                    zo ja,
                        ga voor elk childnode na of deze bestaat
                            zo nee, importeer
                            zo ja, overschrijf waarde
                    zo nee, recurse in (1)
        */
        
        merge(base,newNode, old, true/*, merge*/);
   

        return base;
    }
    
    private static void merge(Document base, Node newNode, Element old, boolean first/*, Node merge*/){
        while (newNode.hasChildNodes()) {
            Node mergeChild = newNode.getFirstChild();
            newNode.removeChild(mergeChild);
            String name = mergeChild.getNodeName();
            NodeList nl = old.getElementsByTagName(name);
            if(nl.getLength() == 0){
                mergeChild = base.importNode(mergeChild, true);
                old.appendChild(mergeChild);
            }else{
                Element oldItem = (Element) nl.item(0);
                if(first){
                    merge(base, mergeChild, oldItem, false);
                }else{
                    if(!mergeChild.getTextContent().equalsIgnoreCase("")){
                        oldItem.setTextContent(mergeChild.getTextContent());
                    }
                }
            }
        }
    }
    
    protected static String print(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }
}
