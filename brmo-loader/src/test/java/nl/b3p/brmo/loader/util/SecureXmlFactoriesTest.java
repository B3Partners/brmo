package nl.b3p.brmo.loader.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.xml.NhrXMLReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

class SecureXmlFactoriesTest {
  @TempDir Path tempDir;

  private String xxe(String root) throws Exception {
    Path secret = Files.writeString(tempDir.resolve("secret.txt"), "SECRET");
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
  void transformerBlocksExternalEntity() throws Exception {
    Transformer t = SecureXmlFactories.newTransformerFactory().newTransformer();
    StringWriter sw = new StringWriter();
    String xml = xxe("r");
    TransformerException e =
        assertThrows(
            TransformerException.class,
            () -> t.transform(new StreamSource(new StringReader(xml)), new StreamResult(sw)));
    assertTrue(e.getMessage().contains("accessExternalDTD"), e.getMessage());
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
  void nhrXMLReaderBlocksExternalEntity() throws Exception {
    String xml = xxe("Envelope");
    Exception e =
        assertThrows(
            Exception.class,
            () ->
                new NhrXMLReader(
                    ReaderInputStream.builder().setReader(new StringReader(xml)).get()));
    assertTrue(e.getMessage().contains("accessExternalDTD"), e.getMessage());
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
