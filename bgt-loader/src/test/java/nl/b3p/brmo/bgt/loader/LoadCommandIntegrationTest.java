/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.extractRowCounts;
import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestFile;
import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadCommandIntegrationTest extends CommandLineTestBase {

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void load(String input, String tables, String dataSetFile) throws DatabaseUnitException, SQLException, IOException {
        String file = getTestFile(input).getAbsolutePath();
        run("load", "--include-history", file);
        assertDataSetEquals(tables, dataSetFile);
    }

    private static Stream<Arguments> load() {
        return Stream.of(
                Arguments.of("bgt_wijk_curve.gml", "wijk", "bgt_wijk_curve"),
                Arguments.of("bgt_openbareruimtelabel.gml", "openbareruimtelabel", "bgt_openbareruimtelabel")
        );
    }

    @Test
    void loadZipFromFile() throws SQLException {
        String file = getTestFile("extract.zip").getAbsolutePath();
        run("load", "--include-history", "--feature-types=all,plaatsbepalingspunt", file);
        assertExtractZipRowCounts(true);
    }

    @Test
    void loadZipFromFileWithoutHistory() throws SQLException {
        String file = getTestFile("extract.zip").getAbsolutePath();
        run("load", "--feature-types=all,plaatsbepalingspunt", file);
        assertExtractZipRowCounts(false);
    }

    @Test
    @EnabledIfSystemProperty(named = "db.connectionString", matches = ".*postgresql.*")
    void postgreSQLNoCopy() throws SQLException {
        String file = getTestFile("extract.zip").getAbsolutePath();
        run("load", "--no-pg-copy", "--include-history", "--feature-types=all,plaatsbepalingspunt", file);
        assertExtractZipRowCounts(true);
    }

    @Test
    void loadZipNoMultithreading() throws SQLException {
        String file = getTestFile("extract.zip").getAbsolutePath();
        run("load", "--no-multithreading", "--include-history", "--feature-types=all,plaatsbepalingspunt", file);
        assertExtractZipRowCounts(true);
    }

    @Test
    void loadZipFromHttp() throws SQLException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody(new Buffer().readFrom(getTestInputStream("extract.zip")))
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/file.zip").toString();
        // MockWebServer does not support partial content
        run("load", "--no-http-zip-random-access", "--include-history", "--feature-types=all,plaatsbepalingspunt", url);
        assertExtractZipRowCounts(true);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS) // Does not have NL overheid SSL root certificate
    void loadZipUsingHttpRandomAccessPDOKOnlineTest() throws SQLException {
        String url = "https://api.pdok.nl/lv/bgt/download/v1_0/delta/predefined/bgt-citygml-nl-delta.zip";
        Object[][] featureTypes = new Object[][] {
                {"openbareruimte", 9526},
                {"waterinrichtingselement",64146}
        };
        String featureTypesString = Stream.of(featureTypes).map(e -> (String)e[0]).collect(Collectors.joining(","));
        run("load", "--http-zip-random-access", "--debug-http-seeks", "--include-history", "--feature-types=" + featureTypesString, url);

        for(Object[] featureType: featureTypes) {
            String table = (String) featureType[0];
            int minRowCount = (int) featureType[1];
            Integer actualRowCount = dbTestConnection.getRowCount(table);
            assertTrue(actualRowCount >= minRowCount,
                    String.format("Expected a minimum row count of %d for table %s, actual %d",
                            minRowCount,
                            table,
                            actualRowCount)
            );
        }
    }


    private void assertExtractZipRowCounts(boolean withHistory) throws SQLException {
        // Contents are compared in DownloadCommandMockIntegrationTest, here only compare row counts
        for(BGTTestFiles.BGTRowCount tableRowCount: extractRowCounts) {
            Integer expectedRowCount = withHistory ? tableRowCount.rowCountWithHistory : tableRowCount.rowCountWithoutHistory;
            Integer actualRowCount = dbTestConnection.getRowCount(tableRowCount.table);
            assertEquals(expectedRowCount, actualRowCount, "Expected row count for table %s" + tableRowCount.table);
        }
    }
}
