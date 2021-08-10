/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;
import okio.Buffer;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.getTestInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrates with loading BGT into a database but mocks the PDOK download service.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DownloadCommandMockIntegrationTest extends CommandLineTestBase {
    @Test
    @Order(1)
    void downloadInitialCompareThreeRows() throws IOException, InterruptedException, SQLException, DatabaseUnitException {
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
        run("download initial", "--no-http-zip-random-access", "--download-service=" + url, "--feature-types=all,plaatsbepalingspunt", "--geo-filter=POLYGON ((131021 458768, 131021 459259, 131694 459259, 131694 458768, 131021 458768))", "--max-objects=3", "--include-history");

        RecordedRequest recordedRequest =  mockWebServer.takeRequest(0, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        String body = recordedRequest.getBody().readString(Charset.defaultCharset());
        assertTrue(body.contains("\"format\":\"citygml\""));
        assertDataSetEquals("kast,ondersteunendwaterdeel,paal,sensor,begroeidterreindeel,overbruggingsdeel,spoor," +
                "bord,pand,nummeraanduidingreeks,functioneelgebied,onbegroeidterreindeel,gebouwinstallatie,weginrichtingselement," +
                "kunstwerkdeel,waterinrichtingselement,installatie,vegetatieobject,waterdeel,put,scheiding,ondersteunendwegdeel" +
                "straatmeubilair,wegdeel,plaatsbepalingspunt,bak,openbareruimtelabel,overigbouwwerk,overigescheiding",
                "extract");
    }

    @Test
    @Order(2)
    @SkipDropTables
    void downloadUpdate() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody(new Buffer().readFrom(getTestInputStream("deltas.json")))
                .setResponseCode(200)
        );
        String url = mockWebServer.url("/").toString();
        run("download update", "--no-http-zip-random-access", "--download-service=" + url);

        RecordedRequest recordedRequest =  mockWebServer.takeRequest(0, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals( "//delta?page=1&count=100", recordedRequest.getPath());

        // TODO apply mutaties
    }

    @Test
    @Order(3)
    void catchApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid wktString. a polygon ring must have at least 4 points, got 1")
                .setHeader("Content-Type", "text/plain; charset=utf-8")
                .setResponseCode(400)
        );
        String url = mockWebServer.url("/").toString();
        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        int exitCode = cmd.execute(getArgs("download initial", "--geo-filter=POLYGON((123 456))", "--download-service=" + url));
        assertEquals(1, exitCode);
        assertThat(sw.toString(), containsString("deltaCustomDownload call failed with: 400 - invalid wktString"));
    }
}
