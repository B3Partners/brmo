/*
 * Copyright (C) 2021 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atteo.xmlcombiner.XmlCombiner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RsgbWOZTransformer extends RsgbTransformer {
  private static final Log LOG = LogFactory.getLog(RsgbWOZTransformer.class);
  private static final String STUF_GEEN_WAARDE = "geenWaarde";
  private final StagingProxy staging;

  public RsgbWOZTransformer(String pathToXsl, StagingProxy staging)
      throws TransformerConfigurationException, ParserConfigurationException {
    super(pathToXsl);
    this.staging = staging;
  }

  protected static Document merge(String oldDBxml, String newDBxml)
      throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    final XPathExpression expression = XPathFactory.newInstance().newXPath().compile("/root/data");

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    // to prevent XXE
    docBuilderFactory.setExpandEntityReferences(false);
    docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    docBuilderFactory.setIgnoringElementContentWhitespace(true);
    docBuilderFactory.setNamespaceAware(false);

    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document base = docBuilder.parse(new InputSource(new StringReader(oldDBxml)));

    Element old = (Element) expression.evaluate(base, XPathConstants.NODE);
    if (old == null) {
      throw new IOException(oldDBxml + ": expression does not evaluate to node");
    }
    LOG.trace("oldDBxml bericht is: " + oldDBxml);
    LOG.trace("newDBxml bericht is: " + newDBxml);
    Document merge = docBuilder.parse(new InputSource(new StringReader(newDBxml)));

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
    XmlCombiner combiner = new XmlCombiner();
    combiner.combine(base);
    combiner.combine(merge);
    return combiner.buildDocument();
  }

  protected static String print(Document doc) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    // to prevent XXE
    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    DOMSource source = new DOMSource(doc);
    StringWriter sw = new StringWriter();
    Result result = new StreamResult(sw);
    transformer.transform(source, result);
    return sw.toString();
  }

  @Override
  public String transformToDbXml(Bericht bericht)
      throws SAXException, IOException, TransformerException {
    String current = super.transformToDbXml(bericht);
    StringBuilder loadLog = new StringBuilder();

    LOG.debug("actuele bericht is: " + bericht);
    try {
      Bericht previousBericht = staging.getPreviousBericht(bericht, loadLog);
      if (previousBericht != null) {
        LOG.debug("vorige bericht is: " + previousBericht);
        if (null == previousBericht.getDbXml()) {
          // TODO mogelijk op te lossen door previousBericht nogmaals te transformeren via
          //      oldDBXml = super.transformToDbXml(previousBericht);
          //      voor nu zetten we de pipelining.enabled op false in de
          // web.xml/context.xml
          //      .
          //      Het probleem is dat het vorige bericht nog in de pipeline kan zitten en
          // niet committed
          //      is naar de bericht tabel
          LOG.warn("Er is wel een vorig bericht, maar de DB_XML ontbreekt");
        }
        Document d = merge(previousBericht.getDbXml(), current);

        String mergedDBXML = print(d);
        bericht.setDbXml(mergedDBXML);
        LOG.trace("mergedDBXML bericht is: " + mergedDBXML);
        current = mergedDBXML;
      }
    } catch (SQLException
        | XPathExpressionException
        | ParserConfigurationException
        | IOException
        | SAXException ex) {
      LOG.error("Vorige bericht kon niet worden opgehaald: ", ex);
    }
    LOG.debug(loadLog);
    return current;
  }
}
