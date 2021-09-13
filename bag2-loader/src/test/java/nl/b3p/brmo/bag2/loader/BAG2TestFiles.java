/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BAG2TestFiles {
    public static InputStream getTestInputStream(String name) {
        InputStream input = BAG2TestFiles.class.getResourceAsStream("/" + name);
        assertNotNull(input, name);
        return input;
    }

    public static File getTestFile(String name) {
        URL url = BAG2TestFiles.class.getResource("/" + name);
        assertNotNull(url, name);
        return new File(url.getFile());
    }

}
