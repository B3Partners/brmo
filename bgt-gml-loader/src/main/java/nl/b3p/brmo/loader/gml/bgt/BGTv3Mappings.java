/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.gml.bgt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author matthijsln
 */
public class BGTv3Mappings {
    XPathFactory xPathfactory = XPathFactory.newInstance();
    Document datamodel;
    XPath xpathFactory = xPathfactory.newXPath();

    Map<String,BGTv3Mapping> mappings = new HashMap();

    public BGTv3Mappings() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        datamodel = builder.parse(BGTv3Mappings.class.getResource("/xml/datamodel.xml").toString());
    }

    public BGTv3Mapping getMapping(String objectType) throws XPathExpressionException {
        objectType = objectType.toLowerCase();

        if(mappings.containsKey(objectType)) {
            return mappings.get(objectType);
        }

        XPathExpression xpath = xpathFactory.compile("//objecttype[@bgtv3='true' and translate(@clazz,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcedfghijklmnopqrstuvwxyz')='" + objectType + "']/@table");
        String table = (String)xpath.evaluate(datamodel, XPathConstants.STRING);
        if(table == null) {
            return null;
        }
        xpath = xpathFactory.compile("//objecttype[@bgtv3='true' and translate(@clazz,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcedfghijklmnopqrstuvwxyz')='" + objectType + "']/attribuut");
        NodeList list = (NodeList)xpath.evaluate(datamodel, XPathConstants.NODESET);
        if(list.getLength() == 0) {
            return null;
        }
        BGTv3Mapping mapping = new BGTv3Mapping(objectType, table, list);
        mappings.put(objectType, mapping);
        return mapping;
    }

    public static void main(String... args) throws Exception {
        System.out.println(new BGTv3Mappings().getMapping("begroeidTerreindeel"));
    }
}
