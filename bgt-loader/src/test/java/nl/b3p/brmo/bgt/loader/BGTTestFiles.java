/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BGTTestFiles {
    public static InputStream getTestInputStream(String name) {
        InputStream input = BGTTestFiles.class.getResourceAsStream("/" + name);
        assertNotNull(input, name);
        return input;
    }

    public static File getTestFile(String name) {
        URL url = BGTTestFiles.class.getResource("/" + name);
        assertNotNull(url, name);
        return new File(url.getFile());
    }

    static class BGTRowCount {
        String table;
        int rowCountWithHistory;
        int rowCountWithoutHistory;
    }

    public static List<BGTRowCount> extractRowCounts = Stream.of(new Object[][]{
        {"kast", 47, 42},
        {"ondersteunendwaterdeel", 487, 266},
        {"paal", 2121, 1836},
        {"sensor", 4, 4},
        {"begroeidterreindeel", 3690, 1948},
        {"overbruggingsdeel", 306, 167},
        {"spoor", 39, 39},
        {"bord", 125, 114},
        {"pand", 3720, 2598},
        {"nummeraanduidingreeks", 2838, 1849},
        {"functioneelgebied", 37, 37},
        {"onbegroeidterreindeel", 4552, 2811},
        {"gebouwinstallatie", 40, 39},
        {"weginrichtingselement", 447, 208},
        {"kunstwerkdeel", 318, 93},
        {"waterinrichtingselement", 98, 98},
        {"installatie", 8, 5},
        {"vegetatieobject", 3955, 3565},
        {"waterdeel", 338, 135},
        {"put", 4058, 3877},
        {"scheiding", 2620, 2230},
        {"ondersteunendwegdeel", 2638, 818},
        {"straatmeubilair", 360, 314},
        {"wegdeel", 7462, 3400},
        {"plaatsbepalingspunt", 10, 10},
        {"bak", 62, 53},
        {"openbareruimtelabel", 876, 405},
        {"overigbouwwerk", 484, 176},
        {"overigescheiding", 1, 1},
    })
    .map(value -> {
        BGTRowCount rowCount = new BGTRowCount();
        rowCount.table = (String) value[0];
        rowCount.rowCountWithHistory = (int) value[1];
        rowCount.rowCountWithoutHistory = (int) value[2];
        return rowCount;
    })
    .collect(Collectors.toList());
}
