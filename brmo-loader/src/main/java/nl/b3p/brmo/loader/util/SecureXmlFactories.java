package nl.b3p.brmo.loader.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.apache.commons.xml.secure.SecureDocumentBuilderFactory;
import org.apache.commons.xml.secure.SecureTransformerFactory;
import org.apache.commons.xml.secure.SecureXMLInputFactory;

/**
 * Factories for XML processing which are hardened against XXE attacks using Apache Commons Secure
 * XML, which resolves external references to empty content. On top of that the {@link
 * DocumentBuilderFactory} and {@link XMLInputFactory} reject documents with a DOCTYPE, so a
 * malicious document fails instead of being silently processed.
 */
public final class SecureXmlFactories {
  private SecureXmlFactories() {}

  /**
   * Create a {@link TransformerFactory} which resolves external entities, DTDs, stylesheets and
   * documents to empty content and has secure processing enabled (entity expansion limits, no
   * extension functions). Stylesheets loaded via a {@link javax.xml.transform.URIResolver} set on
   * the factory are still allowed.
   *
   * @return hardened transformer factory
   * @throws TransformerConfigurationException if the implementation does not support secure
   *     processing
   */
  public static TransformerFactory newTransformerFactory()
      throws TransformerConfigurationException {
    TransformerFactory tf = SecureTransformerFactory.newInstance();
    tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
    DocumentBuilderFactory dbf = SecureDocumentBuilderFactory.newInstance();
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    return dbf;
  }

  /**
   * Create a {@link XMLInputFactory} which does not support DTDs, so no external entities, external
   * DTDs or entity expansion (billion laughs) are possible.
   *
   * @return hardened StAX input factory
   */
  public static XMLInputFactory newXMLInputFactory() {
    XMLInputFactory xif = SecureXMLInputFactory.newInstance();
    xif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    return xif;
  }
}
