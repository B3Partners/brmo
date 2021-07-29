/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrates with loading BGT into a database but mocks the PDOK download service.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DownloadCommandMockIntegrationTest extends CommandLineTestBase {

    @Override
    void tearDown() throws SQLException {
        // Do not drop tables
    }

    @Test
    @Order(1)
    void downloadInitial() throws IOException, InterruptedException, SQLException, DatabaseUnitException {
        MockWebServer mockWebServer = new MockWebServer();

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"_links\":{\"status\":{\"href\":\"/lv/bgt/download/v1_0/delta/custom/d396d842-3963-4377-55e1-0e9a91e1de01/status\"}},\"downloadRequestId\":\"d396d842-3963-4377-55e1-0e9a91e1de01\"}")
                .setResponseCode(202)
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"_links\":{\"self\":{\"href\":\"/lv/bgt/download/v1_0/delta/custom/d396d842-3963-4377-55e1-0e9a91e1de01/status\"}},\"progress\":0,\"status\":\"PENDING\"}")
                .setResponseCode(200)
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"_links\":{\"self\":{\"href\":\"/lv/bgt/download/v1_0/delta/custom/d396d842-3963-4377-55e1-0e9a91e1de01/status\"}},\"progress\":38,\"status\":\"RUNNING\"}")
                .setResponseCode(200)
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"_links\":{\"download\":{\"href\":\"/lv/bgt/download/v1_0/extract/c31c1c7e-a59d-4357-991d-dcc68069d05e/extract.zip\"}},\"progress\":100,\"status\":\"COMPLETED\"}")
                .setResponseCode(201)
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody(new Buffer().readFrom(getTestInputStream("extract.zip")))
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/").toString();
        run("download initial", "--download-service=" + url, "--no-geo-filter", "--include-history", "--feature-types=stadsdeel,waterschap");

        String body = mockWebServer.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(body.contains("\"format\":\"citygml\""));
        assertDataSetEquals("stadsdeel,waterschap", "extract");

        mockWebServer.close();
    }

    @Test
    @Order(2)
    void downloadUpdate() throws IOException, InterruptedException {
        MockWebServer mockWebServer = new MockWebServer();

        mockWebServer.enqueue(new MockResponse()
                .setBody(new Buffer().readFrom(getTestInputStream("deltas.json")))
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/").toString();
        run("download update", "--download-service=" + url);

        assertEquals( "//delta?page=1&count=100", mockWebServer.takeRequest().getPath());

        // TODO apply mutaties
    }
}
