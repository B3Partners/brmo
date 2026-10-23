package nl.b3p.brmo.loader.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

class SecureXmlFactoriesTest {
  private static final String SECRET = "SECRET";

  @TempDir Path tempDir;

  private String xxe(String root) throws Exception {
    Path secret = Files.writeString(tempDir.resolve("secret.txt"), SECRET);
    return "<?xml version=\"1.0\"?><!DOCTYPE "
        + root
        + " [<!ENTITY x SYSTEM \""
        + secret.toUri()
        + "\">]><"
        + root
        + ">&x;</"
        + root
        + ">";
  }

  @Test
  void transformerDoesNotResolveExternalEntity() throws Exception {
    Transformer t = SecureXmlFactories.newTransformerFactory().newTransformer();
    t.setOutputProperty("omit-xml-declaration", "yes");
    StringWriter sw = new StringWriter();
    t.transform(new StreamSource(new StringReader(xxe("r"))), new StreamResult(sw));
    assertFalse(sw.toString().contains(SECRET), "External entity resolved");
    assertEquals("<r/>", sw.toString());
  }

  @Test
  void transformerAllowsPlainXml() throws Exception {
    Transformer t = SecureXmlFactories.newTransformerFactory().newTransformer();
    t.setOutputProperty("omit-xml-declaration", "yes");
    StringWriter sw = new StringWriter();
    t.transform(new StreamSource(new StringReader("<r>ok</r>")), new StreamResult(sw));
    assertEquals("<r>ok</r>", sw.toString());
  }

  @Test
  void templatesWithURIResolverDoNotResolveExternalEntity() throws Exception {
    // Like the brmo XSL stylesheets: xsl:import resolved from the classpath by a URIResolver
    String imported =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:template match=\"r\"><out><xsl:value-of select=\".\"/></out></xsl:template>"
            + "</xsl:stylesheet>";
    String main =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:import href=\"imported.xsl\"/></xsl:stylesheet>";
    TransformerFactory tf = SecureXmlFactories.newTransformerFactory();
    tf.setURIResolver((href, base) -> new StreamSource(new StringReader(imported)));
    Transformer t = tf.newTemplates(new StreamSource(new StringReader(main))).newTransformer();
    t.setOutputProperty("omit-xml-declaration", "yes");
    StringWriter sw = new StringWriter();
    t.transform(new StreamSource(new StringReader(xxe("r"))), new StreamResult(sw));
    assertFalse(sw.toString().contains(SECRET), "External entity resolved");
    assertEquals("<out/>", sw.toString());
  }

  @Test
  void documentBuilderRejectsDoctype() throws Exception {
    DocumentBuilder db = SecureXmlFactories.newDocumentBuilderFactory().newDocumentBuilder();
    String xml = xxe("r");
    SAXParseException e =
        assertThrows(
            SAXParseException.class, () -> db.parse(new InputSource(new StringReader(xml))));
    assertTrue(e.getMessage().contains("DOCTYPE"), e.getMessage());
  }

  @Test
  void documentBuilderRejectsBillionLaughs() throws Exception {
    DocumentBuilder db = SecureXmlFactories.newDocumentBuilderFactory().newDocumentBuilder();
    String xml =
        "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY a \"aaaaaaaaaa\">"
            + "<!ENTITY b \"&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;\">]><r>&b;</r>";
    assertThrows(SAXParseException.class, () -> db.parse(new InputSource(new StringReader(xml))));
  }

  @Test
  void documentBuilderAllowsPlainXml() throws Exception {
    DocumentBuilder db = SecureXmlFactories.newDocumentBuilderFactory().newDocumentBuilder();
    assertEquals(
        "ok",
        db.parse(new InputSource(new StringReader("<r>ok</r>")))
            .getDocumentElement()
            .getTextContent());
  }

  @Test
  void xmlInputFactoryRejectsEntities() throws Exception {
    String xml = xxe("r");
    XMLStreamReader r =
        SecureXmlFactories.newXMLInputFactory().createXMLStreamReader(new StringReader(xml));
    assertThrows(
        XMLStreamException.class,
        () -> {
          while (r.hasNext()) {
            r.next();
          }
        });
  }

  @Test
  void xmlInputFactoryAllowsPlainXml() throws Exception {
    XMLStreamReader r =
        SecureXmlFactories.newXMLInputFactory()
            .createXMLStreamReader(new StringReader("<r>ok</r>"));
    r.nextTag();
    assertEquals("ok", r.getElementText());
  }
}
