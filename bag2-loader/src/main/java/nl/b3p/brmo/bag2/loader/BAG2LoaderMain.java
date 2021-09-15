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
import nl.b3p.brmo.util.CountingSeekableByteChannel;
import nl.b3p.brmo.util.http.HttpSeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.util.logging.Logging;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2LoaderMain {
    private static Log log;

    public static void configureLogging() {
        PropertyConfigurator.configure(BAG2LoaderMain.class.getResourceAsStream("/bag2-loader-cli-log4j.properties"));
        log = LogFactory.getLog(BAG2LoaderMain.class);
        try {
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void main(String... args) throws Exception {
        configureLogging();

        for(String filename: args) {
            if (filename.startsWith("http://") || filename.startsWith("https://")) {
                loadBAG2LeveringHttpZip(filename);
            } else if(filename.endsWith(".zip")) {
                loadBAG2LEveringZipFile(filename);
            } else {
                throw new IllegalArgumentException("Invalid argument: " + filename);
            }
        }
    }

    private static void loadBAG2LeveringHttpZip(String uri) throws Exception {
        Instant allStart = Instant.now();
        int allCount = 0;
        boolean debugHttpSeeks = false;
        try(
                HttpSeekableByteChannel channel = new HttpSeekableByteChannel(new URI(uri)).withDebug(debugHttpSeeks);
                CountingSeekableByteChannel loggingChannel = new CountingSeekableByteChannel(channel);
                org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(
                        loggingChannel,
                        uri,
                        "UTF8",
                        false,
                        true
                );
                Connection connection = DriverManager.getConnection("jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true", "bag", "bag");
        ) {
            if (debugHttpSeeks) {
                System.out.println();
            }
            List<ZipArchiveEntry> selectedEntries = new ArrayList();
            for (Iterator<ZipArchiveEntry> it = zipFile.getEntries().asIterator(); it.hasNext(); ) {
                ZipArchiveEntry entry = it.next();
                if (entry.getName().matches("9999(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.zip")) { // VBO ??
                    selectedEntries.add(entry);
                }
            }
            Long totalBytes = selectedEntries.stream().map(ZipEntry::getSize).reduce(0L, Long::sum);
            long totalRead = 0;

            for(ZipArchiveEntry selectedEntry: selectedEntries) {
                BAG2ObjectTableWriter writer = new BAG2ObjectTableWriter(connection, new PostGISDialect(), BAG2SchemaMapper.getInstance());
                writer.setCreateSchema(true);
                writer.start();

                System.out.print(selectedEntry.getName() + "... ");
                try (
                        CountingInputStream counter = new CountingInputStream(zipFile.getInputStream(selectedEntry));
                        ZipArchiveInputStream zip = new org.apache.commons.compress.archivers.zip.ZipArchiveInputStream(counter)) {

                    ZipEntry entry = zip.getNextZipEntry();
                    Instant start = Instant.now();
                    int count = 0;
                    while(entry != null) {
                        System.out.printf("\r%s: %s (%.1f %%, total %.1f %%)", selectedEntry.getName(), entry.getName(),
                                (100.0 / selectedEntry.getSize()) * counter.getByteCount(),
                                (100.0 / totalBytes) * (totalRead  + counter.getByteCount()));
                        writer.write(zip);
                        count++;
                        entry = zip.getNextZipEntry();
                    }
                    System.out.print("... creating indexes and primary keys...");
                    writer.complete();
                    allCount += writer.getProgress().getObjectCount();
                    totalRead += selectedEntry.getSize();
                    System.out.printf("\r%s: loaded %,d files, %,d total objects in %s%s\n", selectedEntry.getName(), count, writer.getProgress().getObjectCount(), formatTimeSince(start), " ".repeat(50));
                } catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception, trying to continue with next ZIP...");
                }
            }
        }
        System.out.printf("All zips: loaded %d total objects in %s\n", allCount, allCount, formatTimeSince(allStart));
    }

    private static void loadBAG2LEveringZipFile(String filename) throws Exception {
        Instant start = Instant.now();
        int count = 0;
        int maxFiles = -1;

        try (ZipFile zip = new ZipFile(filename);
             Connection connection = DriverManager.getConnection("jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true", "bag", "bag")
        ) {
            BAG2ObjectTableWriter writer = new BAG2ObjectTableWriter(connection, new PostGISDialect(), BAG2SchemaMapper.getInstance());
            writer.setUsePgCopy(false);
            writer.setCreateSchema(true);
            writer.start();

            for (ZipEntry entry: zip.stream().collect(Collectors.toList())) {
                System.out.print("\r" + entry.getName());
                writer.write(zip.getInputStream(entry));

                count++;
                if (count == maxFiles) {
                    break;
                }
            }

            writer.complete();
            System.out.printf("\r%s: loaded %,d files, %,d total objects in %s\n", filename, count, writer.getProgress().getObjectCount(), formatTimeSince(start));
        }
    }
}
