/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Brk2Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Testcases voor {@link Brk2SnapshotXMLReader}.
 *
 * @author mprins
 */
class Brk2SnapshotXMLReaderTest {

    private static final Log LOG = LogFactory.getLog(Brk2SnapshotXMLReaderTest.class);

    /**
     * Een stream met testgevallen voor de {@link Brk2SnapshotXMLReader} methodes.
     * Ieder testgeval bevat 1 kadastraal object.
     *
     * @return een stream met testgevallen
     */
    static Stream<Arguments> enkelObjectBerichtProvider() {
        return Stream.of(
                // berichtXml, objectRef, datum, volgordeNummer
                arguments("/brk2/MUTKX02-ABG00F1856-20211012-1.anon.xml", "NL.IMKAD.KadastraalObject:5260185670000", "2021-10-12", 1),
                arguments("/brk2/MUTKX02-ABG00F1856-20211102-1.anon.xml", "NL.IMKAD.KadastraalObject:5260185670000", "2021-11-02", 1),
                // stand
                arguments("/brk2/stand-perceel-1.anon.xml", "NL.IMKAD.KadastraalObject:50247970000", "2021-06-10", -1),
                // vervallen
                arguments("/brk2/vervallen-5230168170000-2.anon.xml", "NL.IMKAD.KadastraalObject:5230168170000", "2020-12-01", 2)
        );
    }

    /**
     * Test bestand met 1 object.
     *
     * @throws Exception if any
     */
    @ParameterizedTest(name = "test een enkel-bericht XML #{index}: bestand: {0}")
    @MethodSource("enkelObjectBerichtProvider")
    void testEnkeleMutatieInBericht(final String berichtXml, final String objectRef, String datum, int volgordeNummer) throws Exception {
        Brk2SnapshotXMLReader bReader = new Brk2SnapshotXMLReader(Brk2SnapshotXMLReader.class.getResourceAsStream(berichtXml));
        assertTrue(bReader.hasNext(), "Er is geen volgend bericht");
        Brk2Bericht brk2 = bReader.next();

        Date d = new SimpleDateFormat("yyyy-MM-dd").parse(datum);
        assertAll("brk bericht",
                () -> assertNotNull(brk2),
                () -> assertEquals(BrmoFramework.BR_BRK2, brk2.getSoort()),
                () -> assertEquals(volgordeNummer, brk2.getVolgordeNummer(), () -> "Bericht heeft niet de verwachte volgordeNummer: " + volgordeNummer),
                () -> assertEquals(objectRef, brk2.getObjectRef(), () -> "Bericht heeft niet de verwachte objectRef " + objectRef),
                () -> assertEquals(d, brk2.getDatum(), () -> "Bericht heeft niet de verwachte datum: " + datum)
        );

        if (volgordeNummer == -1) {
            assertEquals("stand levering " + datum.replace("-", ""),
                    brk2.getRestoredFileName(d, brk2.getVolgordeNummer()),
                    "Bericht heeft niet de verwachte restoredFileName"
            );
        } else if (volgordeNummer > 1) {
            // skip restoredFileName test voor vervallen
        } else {
            assertEquals(berichtXml.substring(6).replace("anon.xml", "zip"),
                    brk2.getRestoredFileName(d, brk2.getVolgordeNummer()),
                    "Bericht heeft niet de verwachte restoredFileName"
            );
        }

        LOG.trace(brk2);
        assertFalse(bReader.hasNext(), "Er is nog een volgend bericht");
    }
}
