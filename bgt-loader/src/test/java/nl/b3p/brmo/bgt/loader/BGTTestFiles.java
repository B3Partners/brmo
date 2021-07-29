/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BGTTestFiles {
    static InputStream getTestFile(String name) {
        InputStream input = BGTObjectStreamerTest.class.getResourceAsStream("/nl/b3p/brmo/bgt/loader/" + name);
        assertNotNull(input, name);
        return input;
    }
}
