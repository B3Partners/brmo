/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.io.ParseException;

import java.io.File;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BAG2LoaderUtilsTest {

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void analyzeBAG2FileName(String name, BAG2LoaderUtils.BAG2FileName expected) {
        assertEquals(expected, BAG2LoaderUtils.analyzeBAG2FileName(name));
    }

    @Test
    void analyzeBAG2FileNameError() {
        assertThrows(IllegalArgumentException.class, () -> BAG2LoaderUtils.analyzeBAG2FileName("bla.zip"));
    }

    private static Stream<Arguments> analyzeBAG2FileName() {
        BAG2LoaderUtils.BAG2FileName nlStand = new BAG2LoaderUtils.BAG2FileName("lvbag-extract-nl.zip", true, false, false);
        return Stream.of(
                Arguments.of("lvbag-extract-nl.zip", nlStand),
                Arguments.of("https://some.host/path/lvbag-extract-nl.zip", nlStand),
                Arguments.of("http://other.host/path/subpath/lvbag-extract-nl.zip", nlStand),
                Arguments.of("https://extracten.bag.kadaster.nl/lvbag/extracten/Nederland%20LVC/BAGNLDL-08112021.zip", new BAG2LoaderUtils.BAG2FileName("BAGNLDL-08112021.zip", true, false, false)),
                Arguments.of("some" + File.separator + "local" + File.separator + "path" + File.separator + "lvbag-extract-nl.zip", nlStand),
                Arguments.of("BAGGEM0344L-08112021.zip", new BAG2LoaderUtils.BAG2FileName("BAGGEM0344L-08112021.zip", true, true, false, "0344")),
                Arguments.of("BAGGEM0344M-08112021-08122021.zip", new BAG2LoaderUtils.BAG2FileName("BAGGEM0344M-08112021-08122021.zip", false, true, true, "0344", LocalDate.of(2021, 11, 8), LocalDate.of(2021, 12, 8))),
                Arguments.of("BAGNLDM-08112021-08122021.zip", new BAG2LoaderUtils.BAG2FileName("BAGNLDM-08112021-08122021.zip", false, false, true, null, LocalDate.of(2021, 11, 8), LocalDate.of(2021, 12, 8))),
                Arguments.of("BAGNLDM-08112021-09112021.zip", new BAG2LoaderUtils.BAG2FileName("BAGNLDM-08112021-09112021.zip", false, false, false, null, LocalDate.of(2021, 11, 8), LocalDate.of(2021, 11, 9))),
                Arguments.of("BAGNLDM-31102021-01112021.zip", new BAG2LoaderUtils.BAG2FileName("BAGNLDM-31102021-01112021.zip", false, false, false, null, LocalDate.of(2021, 10, 31), LocalDate.of(2021, 11, 1)))
        );
    }
}