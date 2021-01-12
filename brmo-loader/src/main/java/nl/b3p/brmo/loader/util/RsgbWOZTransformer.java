/*
 * Copyright (C) 2021 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;

public class RsgbWOZTransformer extends RsgbTransformer {
    private static final Log LOG = LogFactory.getLog(RsgbWOZTransformer.class);
    private static final String GEEN_WAARDE = "geenWaarde";
    private final StagingProxy staging;

    public RsgbWOZTransformer(String pathToXsl, StagingProxy staging) throws TransformerConfigurationException, ParserConfigurationException {
        super(pathToXsl);
        this.staging = staging;
    }

    protected static Document merge(String oldFile, String newFile) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expression = xpath.compile("/root/object");

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        // to prevent XXE
        docBuilderFactory.setExpandEntityReferences(false);
        docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        docBuilderFactory.setNamespaceAware(true);

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
        merge(base, newNode, old, true/*, merge*/);

        return base;
    }

    private static void merge(Document base, Node newNode, Element old, boolean first/*, Node merge*/) {
        while (newNode.hasChildNodes()) {
            Node newChild = newNode.getFirstChild();
            newNode.removeChild(newChild);
            String name = newChild.getNodeName();
            NodeList nl = old.getElementsByTagName(name);
            if (nl.getLength() == 0) { // Geen oude data gevonden voor huidige node
                newChild = base.importNode(newChild, true);
                newChild.setTextContent(newChild.getTextContent());
                old.appendChild(newChild);
            } else {
                Element oldItem = (Element) nl.item(0);
                if (first) {
                    merge(base, newChild, oldItem, false);
                } else {
                    String content = newChild.getTextContent();
                    if (content.equals(GEEN_WAARDE)) {
                        oldItem.setTextContent("");
                    } else if (content.equals("")) {
                        //keep old content
                    } else {
                        oldItem.setTextContent(sanitizeValue(newChild.getTextContent()));
                    }
                }
            }
        }
    }

    private static String sanitizeValue(String val) {
        if (val.contains(GEEN_WAARDE)) {
            String newValue = val.replaceAll(GEEN_WAARDE + " ", "");
            newValue = newValue.replaceAll(GEEN_WAARDE, "");
            return newValue;
        } else {
            return val;
        }
    }

    protected static String print(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // to prevent XXE
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }

    @Override
    public String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerException {
        String current = super.transformToDbXml(bericht);
        StringBuilder loadLog = new StringBuilder();

        LOG.debug("actuele bericht is: " + bericht);
        try {
            Bericht old = staging.getPreviousBericht(bericht, loadLog);
            LOG.debug("oude bericht is: " + old);
            if (old != null) {
                Document d = merge(old.getDbXml(), current);
                String mergedDBXML = print(d);
                bericht.setDbXml(mergedDBXML);
                current = mergedDBXML;
            }
        } catch (SQLException | XPathExpressionException | ParserConfigurationException ex) {
            LOG.error("Vorige bericht kon niet worden opgehaald: ", ex);
        }

        // retrieve old bericht
        // apply current to old
        // return modified dbxml
        return current;
    }

}
