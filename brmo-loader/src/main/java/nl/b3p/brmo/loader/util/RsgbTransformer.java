/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.brmo.loader.entity.Bericht;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author mprins
 */
public interface RsgbTransformer {
    String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException;

    Node transformToDbXmlNode(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException;
}
