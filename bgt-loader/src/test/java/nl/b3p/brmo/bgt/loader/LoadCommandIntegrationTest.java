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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.extractRowCounts;
import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestFile;
import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadCommandIntegrationTest extends CommandLineTestBase {

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource
    void load(String input, String tables, String dataSetFile) throws DatabaseUnitException, SQLException, IOException {
        String file = getTestFile(input).getAbsolutePath();
        run("load", "--include-history", file);
        assertDataSetEquals(tables, dataSetFile);
    }

    private static Stream<Arguments> load() throws ParseException {
        return Stream.of(
                Arguments.of("bgt_wijk_curve.gml", "wijk", "bgt_wijk_curve"),
                Arguments.of("bgt_openbareruimtelabel.gml", "openbareruimtelabel", "bgt_openbareruimtelabel")
        );
    }

    @Test
    void loadZipFromFile() throws DatabaseUnitException, SQLException, IOException {
        String file = getTestFile("extract.zip").getAbsolutePath();
        run("load", "--include-history", "--feature-types=all,plaatsbepalingspunt", file);
        assertExtractZipRowCounts(true);
    }

    @Test
    void loadZipFromHttp() throws DatabaseUnitException, SQLException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody(new Buffer().readFrom(getTestInputStream("extract.zip")))
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/file.zip").toString();
        run("load", "--include-history", "--feature-types=all,plaatsbepalingspunt", url);
        assertExtractZipRowCounts(true);
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
