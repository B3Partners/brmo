/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
        {"kast", 47, 47},
        {"ondersteunendwaterdeel", 487, 47},
        {"paal", 2121, 47},
        {"sensor", 4, 47},
        {"begroeidterreindeel", 3690, 47},
        {"overbruggingsdeel", 306, 47},
        {"spoor", 39, 47},
        {"bord", 125, 47},
        {"pand", 3720, 47},
        {"nummeraanduidingreeks", 2838, 47},
        {"functioneelgebied", 37, 47},
        {"onbegroeidterreindeel", 4552, 47},
        {"gebouwinstallatie", 40, 47},
        {"weginrichtingselement", 447, 47},
        {"kunstwerkdeel", 318, 47},
        {"waterinrichtingselement", 98, 47},
        {"installatie", 8, 47},
        {"vegetatieobject", 3955, 47},
        {"waterdeel", 338, 47},
        {"put", 4058, 47},
        {"scheiding", 2620, 47},
        {"ondersteunendwegdeel", 2638, 47},
        {"straatmeubilair", 360, 47},
        {"wegdeel", 7462, 47},
        {"plaatsbepalingspunt", 10, 47},
        {"bak", 62, 47},
        {"openbareruimtelabel", 876, 47},
        {"overigbouwwerk", 484, 47},
        {"overigescheiding", 1, 47},
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
