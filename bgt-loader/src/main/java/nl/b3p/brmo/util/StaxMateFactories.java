/*
 * Copyright (C) 2026 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.util;

import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.xml.secure.SecureXMLInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.staxmate.SMInputFactory;

/** Factory for StaxMate input factories for parsing GML with StaxMate and GeoTools. */
public final class StaxMateFactories {
  private StaxMateFactories() {}

  /**
   * Create a StaxMate input factory using a StAX implementation which is secured against XXE by
   * Apache Commons Secure XML, coalesces characters and does not support DTDs.
   *
   * <p>The StAX implementation must natively implement Stax2, such as Woodstox. For other
   * implementations (such as the JDK default) StaxMate wraps the reader in a {@link
   * org.codehaus.stax2.ri.Stax2ReaderAdapter} which does not track the element depth for {@link
   * XMLStreamReader#nextTag()}. The GeoTools XmlStreamGeometryReader uses that method, so after
   * reading some geometries (such as curves) StaxMate cursors silently skip the elements after it.
   *
   * <p>The GeoTools geometry reader reads from {@link
   * org.codehaus.staxmate.in.SMInputCursor#getStreamReader()} directly, bypassing the cursors.
   * StaxMate allows this but discourages it, because advancing the reader directly can desync the
   * cursors (see <a href="https://github.com/FasterXML/StaxMate/issues/2">StaxMate issue #2</a>).
   * As of StaxMate 2.4.2 this works because cursors resync using {@code getDepth()} of the reader,
   * which is only correct for all methods with a native Stax2 implementation, and because the
   * geometry is read within the element of a child cursor which is not used afterward.
   *
   * <p>If a future StaxMate version resolves issue #2 (for instance by wrapping the returned reader
   * to keep the cursors synchronized), this check may no longer be needed. Note that StaxMate may
   * be upgraded by automatic dependency updates without anyone checking whether that issue has been
   * resolved, so keep the check unless it has been verified to be unnecessary.
   *
   * @return the StaxMate input factory
   * @throws IllegalStateException if the StAX implementation does not natively implement Stax2
   */
  public static SMInputFactory newSMInputFactory() {
    final XMLInputFactory xmlInputFactory = SecureXMLInputFactory.newFactory();
    xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE); // Coalesce characters
    xmlInputFactory.setProperty(
        XMLInputFactory.SUPPORT_DTD,
        Boolean.FALSE); // No XML entity expansions or external entities

    try {
      XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader("<r/>"));
      if (!(reader instanceof XMLStreamReader2)) {
        throw new IllegalStateException(
            "StAX implementation "
                + reader.getClass().getName()
                + " does not implement Stax2, use Woodstox");
      }
      reader.close();
    } catch (XMLStreamException e) {
      throw new IllegalStateException("Error checking StAX implementation", e);
    }

    return new SMInputFactory(xmlInputFactory);
  }
}
