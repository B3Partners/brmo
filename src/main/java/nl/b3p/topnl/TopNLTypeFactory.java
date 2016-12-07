/*
 * Copyright (C) 2016 B3Partners B.V.
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
package nl.b3p.topnl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class TopNLTypeFactory {

    private final String TOP250NLNAMESPACE = "http://register.geostandaarden.nl/gmlapplicatieschema/top250nl/1.2.0";
    private final String TOP100NLNAMESPACE = "http://register.geostandaarden.nl/gmlapplicatieschema/top100nl/1.1.0";
    private final String TOP50NLNAMESPACE = "http://www.kadaster.nl/top50nl/1.1";
    protected final static Log log = LogFactory.getLog(TopNLTypeFactory.class);

    public TopNLType getTopNLType(InputStream is) throws JDOMException, IOException {
        Document inputXml = new SAXBuilder().build(is);

        if (!inputXml.hasRootElement()) {
            throw new IllegalArgumentException("Document contains no root element");
        }
        Element rootElem = inputXml.getRootElement();
        String currentNamespace = rootElem.getNamespace().getURI();
        switch (currentNamespace) {
            case TOP250NLNAMESPACE:
                return TopNLType.TOP250NL;
            case TOP100NLNAMESPACE:
                return TopNLType.TOP100NL;
            case TOP50NLNAMESPACE:
                return TopNLType.TOP50NL;
            default:
                throw new IllegalArgumentException("Type not recognized: " + currentNamespace);
        }
    }

}
