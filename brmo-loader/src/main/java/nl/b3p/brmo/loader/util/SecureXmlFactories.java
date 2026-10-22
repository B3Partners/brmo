package nl.b3p.brmo.loader.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/** Factories for XML processing which are hardened against XXE attacks. */
public final class SecureXmlFactories {
  private SecureXmlFactories() {}

  /**
   * Create a {@link TransformerFactory} which does not load external DTDs or external stylesheets
   * and has secure processing enabled (entity expansion limits, no extension functions).
   * Stylesheets loaded via a {@link javax.xml.transform.URIResolver} set on the factory are still
   * allowed.
   *
   * @return hardened transformer factory
   * @throws TransformerConfigurationException if the implementation does not support secure
   *     processing
   */
  public static TransformerFactory newTransformerFactory()
      throws TransformerConfigurationException {
    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    return tf;
  }

  /**
   * Create a {@link DocumentBuilderFactory} which rejects documents with a DOCTYPE declaration, so
   * no external entities, external DTDs or entity expansion (billion laughs) are possible.
   *
   * @return hardened document builder factory
   * @throws ParserConfigurationException if the implementation does not support the required
   *     features
   */
  public static DocumentBuilderFactory newDocumentBuilderFactory()
      throws ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);
    return dbf;
  }
}
