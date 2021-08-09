/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2LoaderMain {
    public static void main(String... args) throws Exception {
        Instant start = Instant.now();
        int count = 0;
        int objects = 0;
        try(ZipFile zip = new ZipFile(args[0])) {
            for(ZipEntry entry: zip.stream().collect(Collectors.toList())) {
                System.out.print(entry.getName()+ ": ");
                objects += new BAG2GMLObjectStream(zip.getInputStream(entry)).load();
                count++;
            }
        }
        System.out.printf("Loaded %,d files, %,d total objects in %s\n", count, objects, formatTimeSince(start));
    }
}
