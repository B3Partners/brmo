/*
* Copyright (C) 2022 B3Partners B.V.
*
* SPDX-License-Identifier: MIT
*
*/
package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.xml.leveringsdocument.BAGExtractLevering;
import nl.b3p.brmo.bag2.xml.leveringsdocument.GebiedRegistratief;
import nl.b3p.brmo.bag2.xml.leveringsdocument.LVCExtract;
import nl.b3p.brmo.bag2.xml.leveringsdocument.SelectieGegevens;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LeveringsdocumentTest {
    @Test
    void testLeveringsdocumentInZip() throws IOException {
        BAG2LoaderUtils.BAGExtractLeveringWrapper levering = BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(BAG2TestFiles.getTestFile("BAGGEM1904L-15102021.zip"));
        assertNotNull(levering);
        LVCExtract lvcExtract = levering.getLVCExtract();
        assertNotNull(lvcExtract);
        assertEquals("2021-10-15", lvcExtract.getStandTechnischeDatum().toString());
        GebiedRegistratief gebiedRegistratief = levering.getGebiedRegistratief();
        assertNotNull(gebiedRegistratief);
        assertNotNull(gebiedRegistratief.getGebiedGEM());
        assertEquals(1, gebiedRegistratief.getGebiedGEM().getGemeenteCollectie().getGemeente().size());
        assertEquals("Stichtse Vecht", gebiedRegistratief.getGebiedGEM().getGemeenteCollectie().getGemeente().get(0).getGemeenteNaam());
    }

    @Test
    void testNoLeveringsdocumentInZip() {
        assertThrows(IOException.class, () -> BAG2LoaderUtils.findAndParseLeveringsdocumentInZip(BAG2TestFiles.getTestFile("1978MUT15082021-15092021-000002.zip")));
    }

    @Test
    void testZipNotFound() {
        assertThrows(IOException.class, () -> BAG2LoaderUtils.findAndParseLeveringsdocumentInZip("doesnotexist.zip"));
    }
}
