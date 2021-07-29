/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.BGTLoaderMain;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.apache.commons.io.IOUtils;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.io.ParseException;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestFile;
import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandLineLoadIntegrationTest extends DBTestBase {

    BGTLoaderMain main;
    CommandLine cmd;

    @BeforeEach
    void init(TestInfo info) {
        main = new BGTLoaderMain();
        main.configureLogging();
        cmd = new CommandLine(main);
        System.out.println("\nExecute test: " + info.getDisplayName());
    }

    void run(String... args) {
        System.out.println("Command line: " + String.join(" ", args));
        int exitCode = cmd.execute(args);
        assertEquals(0, exitCode);
    }

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void load(String input, String[] tables, String dataSetFile) throws DatabaseUnitException, SQLException, IOException {
        String file = getTestFile(input).getAbsolutePath();
        run("load", "--connection=" + connectionString, "--user=" + user, "--password=" + password, "--include-history", file);
        assertDataSetEquals(tables, dataSetFile);
    }

    private static Stream<Arguments> load() throws ParseException {
        return Stream.of(
                Arguments.of("bgt_wijk_curve.gml", new String[]{"wijk"}, "bgt_wijk_curve"),
                Arguments.of("bgt_openbareruimtelabel.gml", new String[]{"openbareruimtelabel"}, "bgt_openbareruimtelabel")
        );
    }

    private byte[] createZip(String[] inputFiles) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bytes);
        for(String file: inputFiles) {
            ZipEntry zipEntry = new ZipEntry(file);
            zos.putNextEntry(zipEntry);
            IOUtils.copy(getTestInputStream(file), zos);
            zos.closeEntry();
        }
        zos.close();
        return bytes.toByteArray();
    }

    @Test
    void loadMultiGmlZipFromFile() throws DatabaseUnitException, SQLException, IOException {
        File file = File.createTempFile("bgt-loader-gml-test",".zip");
        byte[] zip = createZip(
                new String[]{"bgt_openbareruimtelabel.gml", "bgt_waterschap.gml", "bgt_stadsdeel.gml"}
        );
        IOUtils.copy(new ByteArrayInputStream(zip), new FileOutputStream(file));
        run("load", "--connection=" + connectionString, "--user=" + user, "--password=" + password, "--include-history", file.getAbsolutePath());
        assertDataSetEquals(
                new String[]{"openbareruimtelabel", "waterschap", "stadsdeel"},
                new String[]{"bgt_openbareruimtelabel", "bgt_waterschap", "bgt_stadsdeel"}
        );
    }

    @Test
    void loadZipFromHttp() throws DatabaseUnitException, SQLException, IOException {
        MockWebServer mockWebServer = new MockWebServer();
        Buffer body = new Buffer();
        byte[] zip = createZip(new String[]{"bgt_openbareruimtelabel.gml"});
        IOUtils.copy(new ByteArrayInputStream(zip), body.outputStream());
        mockWebServer.enqueue(new MockResponse()
                .setBody(body)
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/file.zip").toString();
        run("load", "--connection=" + connectionString, "--user=" + user, "--password=" + password, "--include-history", url);
        assertDataSetEquals(new String[]{"openbareruimtelabel"}, "bgt_openbareruimtelabel");

        mockWebServer.close();
    }
}
