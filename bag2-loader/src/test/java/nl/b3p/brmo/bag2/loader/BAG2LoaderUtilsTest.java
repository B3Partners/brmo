/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class BAG2LoaderUtilsTest {
    @Test
    void BAGExtractSelectieFromFilename_Error() {
        assertThrows(IllegalArgumentException.class, () -> BAG2LoaderUtils.BAGExtractSelectieFromFilename.parse("bla.zip"));
    }

    private void BAGExtractSelectieFromFilenameIsNLStand(String filename) {
        BAG2LoaderUtils.BAGExtractSelectie selectie = BAG2LoaderUtils.BAGExtractSelectieFromFilename.parse("lvbag-extract-nl.zip");
        assertTrue(selectie.isGebiedNLD());
        assertTrue(selectie.isStand());
    }
    @Test
    void BAGExtractSelectieFromFilename_GebiedNLD() {
        BAGExtractSelectieFromFilenameIsNLStand("lvbag-extract-nl.zip");
    }

    @Test
    void BAGExtractSelectieFromFilename_GebiedNLDFromHttpsURL() {
        BAGExtractSelectieFromFilenameIsNLStand("https://some.host/path/lvbag-extract-nl.zip");
    }

    @Test
    void BAGExtractSelectieFromFilename_GebiedNLDFromHttsURL() {
        BAGExtractSelectieFromFilenameIsNLStand("http://some.host/path/lvbag-extract-nl.zip");
    }

    @Test
    void BAGExtractSelectieFromFilename_GebiedNLDFromFilePath() {
        BAGExtractSelectieFromFilenameIsNLStand("some" + File.separator + "local" + File.separator + "path" + File.separator + "lvbag-extract-nl.zip");
    }

    @Test
    void BAGExtractSelectieFromFilename_GemeenteStand() {
        BAG2LoaderUtils.BAGExtractSelectie selectie = BAG2LoaderUtils.BAGExtractSelectieFromFilename.parse("BAGGEM0344L-08112021.zip");
        assertTrue(selectie.isStand());
        assertFalse(selectie.isGebiedNLD());
        assertEquals(Collections.singleton("0344"), selectie.getGemeenteCodes());
    }

    @Test
    void BAGExtractSelectieFromFilename_GemeenteMutaties() {
        BAG2LoaderUtils.BAGExtractSelectie selectie = BAG2LoaderUtils.BAGExtractSelectieFromFilename.parse("BAGGEM0344M-08112021-08122021.zip");
        assertFalse(selectie.isStand());
        assertFalse(selectie.isGebiedNLD());
        assertEquals(Collections.singleton("0344"), selectie.getGemeenteCodes());
        assertEquals(LocalDate.of(2021, 11, 8), selectie.getMutatiesFrom());
        assertEquals(LocalDate.of(2021, 12, 8), selectie.getMutatiesTot());
    }

    @Test
    void BAGExtractSelectieFromFilename_NLDDagMutaties() {
        BAG2LoaderUtils.BAGExtractSelectie selectie = BAG2LoaderUtils.BAGExtractSelectieFromFilename.parse("BAGNLDM-08112021-08122021.zip");
        assertFalse(selectie.isStand());
        assertTrue(selectie.isGebiedNLD());
        assertEquals(LocalDate.of(2021, 11, 8), selectie.getMutatiesFrom());
        assertEquals(LocalDate.of(2021, 12, 8), selectie.getMutatiesTot());
    }

    @Test
    void BAGExtractSelectieFromLeveringsdocument() throws IOException {
        BAG2LoaderUtils.BAGExtractSelectie selectie = BAG2LoaderUtils.getBAGExtractSelectieFromZipFile(BAG2TestFiles.getTestFile("BAGGEM1904L-15102021.zip"));
        assertNotNull(selectie);
        assertTrue(selectie.isStand());
        assertFalse(selectie.isGebiedNLD());
        assertEquals(Collections.singleton("1904"), selectie.getGemeenteCodes());
    }
}