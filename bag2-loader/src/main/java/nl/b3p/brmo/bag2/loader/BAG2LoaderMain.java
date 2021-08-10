/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.bag2.schema.BAG2SchemaMapper;
import nl.b3p.brmo.sql.dialect.PostGISDialect;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2LoaderMain {
    public static void main(String... args) throws Exception {
        Instant start = Instant.now();
        int count = 0;
        int totalObjects = 0;
        for(String filename: args) {
            try (ZipFile zip = new ZipFile(filename);
                 Connection connection = DriverManager.getConnection("jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true", "bag", "bag")
            ) {
                for (ZipEntry entry: zip.stream().collect(Collectors.toList())) {
                    System.out.print(entry.getName() + ": ");
                    int objects = loadEntry(connection, zip.getInputStream(entry));
                    totalObjects += objects;
                    System.out.println(objects);
                    count++;
                }
            }
            System.out.printf("%s: loaded %,d files, %,d total objects in %s\n", filename, count, totalObjects, formatTimeSince(start));
        }
    }

    private static int loadEntry(Connection connection, InputStream input) throws Exception {
        BAG2ObjectTableWriter writer = new BAG2ObjectTableWriter(connection, new PostGISDialect(), BAG2SchemaMapper.getInstance());
        writer.setCreateSchema(true);
        writer.write(input);
        return (int)writer.getProgress().getObjectCount();
    }
}
