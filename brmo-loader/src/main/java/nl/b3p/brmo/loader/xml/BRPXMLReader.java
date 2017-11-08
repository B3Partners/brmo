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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Meine Toonen
 */
public class BRPXMLReader extends BrmoXMLReader{

    private InputStream in;
    private NodeList nodes = null;
    private int index;
    
    
    public BRPXMLReader(InputStream in, Date d) throws Exception{
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
        
        String brXML = sw.toString();
        Bericht b = new Bericht(brXML);
        b.setSoort(BrmoFramework.BR_BRP);
        b.setDatum(new Date());
        return b;
    }
    
}
