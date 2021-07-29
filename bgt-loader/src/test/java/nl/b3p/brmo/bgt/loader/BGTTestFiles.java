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

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BGTTestFiles {
    static InputStream getTestInputStream(String name) {
        InputStream input = BGTObjectStreamerTest.class.getResourceAsStream("/nl/b3p/brmo/bgt/loader/" + name);
        assertNotNull(input, name);
        return input;
    }

    static File getTestFile(String name) {
        URL url = BGTObjectStreamerTest.class.getResource(name);
        assertNotNull(url, name);
        return new File(url.getFile());
    }
}
