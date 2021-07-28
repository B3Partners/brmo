/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpStartRangeInputStreamProviderTest {

    MockWebServer mockWebServer;
    HttpStartRangeInputStreamProvider provider;

    @BeforeEach
    void init() {
        this.mockWebServer = new MockWebServer();
        this.provider = new HttpStartRangeInputStreamProvider(mockWebServer.url("/").uri());
    }

    @AfterEach
    void close() throws IOException {
        this.mockWebServer.close();
    }

    @Test
    void fromStart() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("test")
                .setResponseCode(200));
        String responseBody = IOUtils.toString(provider.get(0, 0, null), Charset.defaultCharset());
        assertEquals("test", responseBody);
    }

    @Test
    void failedResponse() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("test")
                .setResponseCode(404));
        assertThrows(RuntimeException.class, () -> {
            provider.get(0, 0, null).read();
        }, "HTTP status code: 404");
    }

    @Test
    void requestModifier() throws InterruptedException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("test")
                .setResponseCode(200));
        provider = new HttpStartRangeInputStreamProvider(mockWebServer.url("/").uri(), HttpClient.newHttpClient(), builder -> {
            builder.headers("Test-Header", "Some value");
        });
        provider.get(0, 0, null).read();
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("Some value", request.getHeader("Test-Header"));
    }

    @Test
    void noAcceptRanges() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        provider.get(0, 0, null).read();
        assertThrows(IOException.class, () -> {
            provider.get(1, 0, null).read();
        }, "Exception reading from HTTP server and resume not supported");
    }

    @Test
    void rangeHeaderSent() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(206));
        provider.get(123, 0, null).read();
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("bytes=123-", request.getHeader("Range"));
    }

    @Test
    void ifRangeHeaderSent() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Accept-Ranges", "bytes")
                .addHeader("ETag", "\"something\"")
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(206));
        provider.get(0, 0, null).read();
        provider.get(123, 0, null).read();
        mockWebServer.takeRequest();
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("something", request.getHeader("If-Range"));
    }

    @Test
    void noIfRangePossible() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Accept-Ranges", "bytes")
                .setResponseCode(200));
        provider.get(0, 0, null).read();
        assertThrows(IOException.class, () -> {
            provider.get(1, 0, null).read();
        }, "Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified");
    }

    @Test
    void weakETag() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Accept-Ranges", "bytes")
                .addHeader("ETag", "W/\"weak etag\"")
                .setResponseCode(200));
        provider.get(0, 0, null).read();
        assertThrows(IOException.class, () -> {
            provider.get(123, 0, null).read();
        }, "Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified");
    }

    @Test
    void noPartialResponse() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Accept-Ranges", "bytes")
                .addHeader("ETag", "\"something\"")
                .setResponseCode(200));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));
        provider.get(0, 0, null).read();
        assertThrows(RuntimeException.class, () -> {
            provider.get(123, 0, null).read();
        }, "Error retrying HTTP request at position 123: expected 206 response status but got 200");
    }
}