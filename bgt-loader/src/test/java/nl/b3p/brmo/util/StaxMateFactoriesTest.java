/*
 * Copyright (C) 2026 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import org.codehaus.stax2.ri.Stax2ReaderAdapter;
import org.junit.jupiter.api.Test;

class StaxMateFactoriesTest {
  private static final String FACTORY_PROPERTY = "javax.xml.stream.XMLInputFactory";

  @Test
  void usesNativeStax2Reader() throws Exception {
    assertFalse(
        StaxMateFactories.newSMInputFactory()
                .rootElementCursor(new StringReader("<r/>"))
                .getStreamReader()
            instanceof Stax2ReaderAdapter);
  }

  @Test
  void rejectsJdkStaxImplementation() {
    String previous = System.getProperty(FACTORY_PROPERTY);
    System.setProperty(FACTORY_PROPERTY, "com.sun.xml.internal.stream.XMLInputFactoryImpl");
    try {
      IllegalStateException e =
          assertThrows(IllegalStateException.class, StaxMateFactories::newSMInputFactory);
      assertTrue(e.getMessage().contains("does not implement Stax2"), e.getMessage());
    } finally {
      if (previous == null) {
        System.clearProperty(FACTORY_PROPERTY);
      } else {
        System.setProperty(FACTORY_PROPERTY, previous);
      }
    }
  }
}
