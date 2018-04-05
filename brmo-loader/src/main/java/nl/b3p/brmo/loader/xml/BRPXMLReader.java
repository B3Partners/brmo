/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Meine Toonen
 */
public class BRPXMLReader extends BrmoXMLReader {

    private InputStream in;
    private NodeList nodes = null;
    private int index;
    
    public static final String PREFIX = "NL.BRP.Persoon.";

    public BRPXMLReader(InputStream in, Date d) throws Exception {
        this.in = in;
        setBestandsDatum(d);
        init();
    }

    @Override
    public void init() throws Exception {
        soort = BrmoFramework.BR_BRP;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);
        nodes = doc.getDocumentElement().getChildNodes();
        index = 0;
    }

    @Override
    public boolean hasNext() throws Exception {
        return index < nodes.getLength();
    }

    @Override
    public Bericht next() throws Exception {
        Node n = nodes.item(index);
        index++;

        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(n), new StreamResult(sw));//new StreamResult(outputStream));
 
        Map<String, String> bsns = extractBSN(n);

        String el = getXML(bsns);
        String origXML = sw.toString();
        String brXML = "<root>" + origXML;
        brXML += el + "</root>";
        Bericht b = new Bericht(brXML);
        b.setBrOrgineelXml(origXML);
        b.setVolgordeNummer(index);
        b.setObjectRef(getObjectRef(n));
        b.setSoort(BrmoFramework.BR_BRP);
        b.setDatum(getBestandsDatum());
        return b;
    }

    private String getObjectRef(Node n) {
        NodeList childs = n.getChildNodes();
        String hash = null;
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            String name = child.getNodeName();
            if (name.contains("bsn-nummer")) {
                hash = child.getTextContent();
                hash = getHash(hash);
                break;
            }
        }
        return hash;
    }

    /**
     * maakt een map met bsn,bsnhash.
     * @param n document node met bsn-nummer
     * @return hashmap met bsn,bsnhash
     * @throws XPathExpressionException if any
     */
    public Map<String, String> extractBSN(Node n) throws XPathExpressionException {
        Map<String, String> hashes = new HashMap<>();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[local-name() = 'bsn-nummer']");
        NodeList nodelist = (NodeList) expr.evaluate(n, XPathConstants.NODESET);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node bsn = nodelist.item(i);
            String bsnString = bsn.getTextContent();
            String hash = getHash(bsnString);
            hashes.put(bsnString, hash);
            
        }
        return hashes;
    }

    public String getXML(Map<String, String> map) throws ParserConfigurationException {
        String root = "<bsnhashes>";
        for (Entry<String, String> entry : map.entrySet()) {
            if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                String hash = entry.getValue();
              
                String el = "<" + PREFIX + entry.getKey() + ">" + hash + "</" + PREFIX + entry.getKey() + ">";
                root += el;
            }
        }
        root += "</bsnhashes>";
        return root;
    }
}
