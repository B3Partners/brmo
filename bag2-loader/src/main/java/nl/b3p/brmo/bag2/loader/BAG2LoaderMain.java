/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.schema.BAG2ObjectTableWriter;
import nl.b3p.brmo.bag2.schema.BAG2ObjectType;
import nl.b3p.brmo.bag2.schema.BAG2Schema;
import nl.b3p.brmo.bag2.schema.BAG2SchemaMapper;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.util.logging.Logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2LoaderMain {
    private static Log log;

    private Set<BAG2ObjectType> objectTypesWithSchemaCreated = new HashSet<>();

    private Connection connection;

    private SQLDialect dialect = new OracleDialect();

    public BAG2LoaderMain() {
    }

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

        BAG2LoaderMain loader = new BAG2LoaderMain();

        for(String filename: args) {
            if (filename.startsWith("http://") || filename.startsWith("https://")) {
                loader.loadBAG2ExtractFromStream(filename, new ResumingInputStream(new HttpStartRangeInputStreamProvider(new URI(filename))));
            } else if(filename.endsWith(".zip")) {
                loader.loadBAG2ExtractFromStream(filename, new FileInputStream(filename));
            } else {
                throw new IllegalArgumentException("Invalid argument: " + filename);
            }
        }
    }

    protected Connection getConnection() throws SQLException {
        //return DriverManager.getConnection("jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true", "bag", "bag");
        return DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "c##bag", "bag");
    }

    private void loadBAG2ExtractFromStream(String name, InputStream input) throws Exception {
        Instant start = Instant.now();

        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(input);
             Connection connection = getConnection()
        ) {
            this.connection = connection;

            ZipArchiveEntry entry = zip.getNextZipEntry();
            while(entry != null) {
                if (entry.getName().matches("9999(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.xml")) {
                    // Load extracted zipfile
                    loadXmlEntriesFromZipFile(name, zip, entry, true);
                    break;
                }

                // Process single and double-nested ZIP files

                if (entry.getName().matches("9999(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.zip")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    loadXmlEntriesFromZipFile(entry.getName(), nestedZip, nestedZip.getNextZipEntry(), true);
                }

                if (entry.getName().startsWith("9999Inactief")) {
                    ZipArchiveInputStream nestedZip = new ZipArchiveInputStream(zip);
                    ZipArchiveEntry nestedEntry = nestedZip.getNextZipEntry();
                    while(nestedEntry != null) {
                        if (nestedEntry.getName().startsWith("9999IA")) {
                            ZipArchiveInputStream moreNestedZip = new ZipArchiveInputStream(nestedZip);
                            loadXmlEntriesFromZipFile(nestedEntry.getName(), moreNestedZip, moreNestedZip.getNextZipEntry(), false);
                        }
                        nestedEntry = nestedZip.getNextZipEntry();
                    }
                }

                try {
                    entry = zip.getNextZipEntry();
                } catch(IOException e) {
                    // Reading the ZIP from HTTP may give this error, but it is a normal end...
                    if ("Truncated ZIP file".equals(e.getMessage())) {
                        break;
                    }
                }
            }
            completeAll();
        }
        System.out.println("Total time: " + formatTimeSince(start));
    }

    private static BAG2ObjectType getObjectTypeFromFilename(String filename) {
        Matcher m = Pattern.compile(".*9999(IA)?(STA|VBO|OPR|NUM|LIG|PND|WPL).*\\.(xml|zip)").matcher(filename);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid BAG2 filename: " + filename);
        }
        String objectTypeName = null;
        switch(m.group(2)) {
            case "STA": objectTypeName = "Standplaats"; break;
            case "OPR": objectTypeName = "OpenbareRuimte"; break;
            case "VBO": objectTypeName = "Verblijfsobject"; break;
            case "NUM": objectTypeName = "Nummeraanduiding"; break;
            case "LIG": objectTypeName = "Ligplaats"; break;
            case "PND": objectTypeName = "Pand"; break;
            case "WPL": objectTypeName = "Woonplaats"; break;
        }
        return BAG2Schema.getInstance().getObjectTypeByName(objectTypeName);
    }

    private void completeAll() throws Exception {
        BAG2ObjectTableWriter writer = new BAG2ObjectTableWriter(connection, dialect, BAG2SchemaMapper.getInstance());
        for(BAG2ObjectType objectType: objectTypesWithSchemaCreated) {
            Instant start = Instant.now();
            System.out.print("\r" + objectType.getName() + ": creating keys and indexes...");
            writer.createKeys(objectType); // BAG2 writer is always a single ObjectType unlike BGT
            writer.createIndexes(objectType);
            writer.getConnection().commit();
            System.out.println(" " + formatTimeSince(start));
        }
    }

    private void loadXmlEntriesFromZipFile(String name, ZipArchiveInputStream zip, ZipArchiveEntry entry, boolean createSchema) throws Exception {
        BAG2ObjectTableWriter writer;
        BAG2ObjectType objectType = getObjectTypeFromFilename(name);
        boolean schemaCreated = objectTypesWithSchemaCreated.contains(objectType);
        writer = new BAG2ObjectTableWriter(connection, dialect, BAG2SchemaMapper.getInstance());
        writer.setBatchSize(10000);
        writer.setUsePgCopy(true);
        writer.setCreateSchema(!schemaCreated);
        writer.setCreateKeysAndIndexes(false);
        writer.start(); // sets InitialLoad to true
        writer.getProgress().setInitialLoad(!schemaCreated); // For a COPY in transaction, table must be created or truncated in it

        try {
            int count = 0;
            int maxFiles = -1;
            Instant start = Instant.now();

            while(entry != null) {
                System.out.print("\r" + name + ": " + entry.getName());
                writer.write(CloseShieldInputStream.wrap(zip));
                count++;
                entry = zip.getNextZipEntry();
                if (count == maxFiles) {
                    break;
                }
            }
            writer.complete();

            if (writer.getProgress().getObjectCount() > 0) {
                objectTypesWithSchemaCreated.add(objectType);
            }

            System.out.printf("\r%s: loaded %,d files, %,d total objects in %s\n", name, count, writer.getProgress().getObjectCount(), formatTimeSince(start));
        } catch(Exception e) {
            writer.abortWorkerThread();
            throw e;
        }
    }
}
