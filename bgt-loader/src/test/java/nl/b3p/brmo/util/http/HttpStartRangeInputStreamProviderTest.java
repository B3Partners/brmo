/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import nl.b3p.brmo.util.http.wrapper.Java11HttpClientWrapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    mockWebServer.enqueue(new MockResponse.Builder().body("test").code(200).build());
    String responseBody = IOUtils.toString(provider.get(0, 0, null), Charset.defaultCharset());
    Assertions.assertEquals("test", responseBody);
  }

  @Test
  void failedResponse() {
    mockWebServer.enqueue(new MockResponse.Builder().body("test").code(404).build());
    Assertions.assertThrows(
        RuntimeException.class,
        () -> {
          provider.get(0, 0, null).read();
        },
        "HTTP status code: 404");
  }

  @Test
  void requestModifier() throws InterruptedException, IOException {
    mockWebServer.enqueue(new MockResponse.Builder().body("test").code(200).build());
    provider =
        new HttpStartRangeInputStreamProvider(
            mockWebServer.url("/").uri(),
            new Java11HttpClientWrapper() {
              @Override
              public void beforeRequest(HttpRequest.Builder requestBuilder) {
                requestBuilder.headers("Test-Header", "Some value");
              }
            });
    provider.get(0, 0, null).read();
    RecordedRequest request = mockWebServer.takeRequest();
    Assertions.assertEquals("Some value", request.getHeaders().get("Test-Header"));
  }

  @Test
  void noAcceptRanges() throws IOException {
    mockWebServer.enqueue(new MockResponse.Builder().code(200).build());
    provider.get(0, 0, null).read();
    Assertions.assertThrows(
        IOException.class,
        () -> {
          provider.get(1, 0, null).read();
        },
        "Exception reading from HTTP server and resume not supported");
  }

  @Test
  void rangeHeaderSent() throws IOException, InterruptedException {
    mockWebServer.enqueue(new MockResponse.Builder().code(206).build());
    provider.get(123, 0, null).read();
    RecordedRequest request = mockWebServer.takeRequest();
    Assertions.assertEquals("bytes=123-", request.getHeaders().get("Range"));
  }

  @Test
  void ifRangeHeaderSent() throws IOException, InterruptedException {
    mockWebServer.enqueue(
        new MockResponse.Builder()
            .addHeader("Accept-Ranges", "bytes")
            .addHeader("ETag", "\"something\"")
            .code(200)
            .build());
    mockWebServer.enqueue(new MockResponse.Builder().code(206).build());
    provider.get(0, 0, null).read();
    provider.get(123, 0, null).read();
    mockWebServer.takeRequest();
    RecordedRequest request = mockWebServer.takeRequest();
    Assertions.assertEquals("\"something\"", request.getHeaders().get("If-Range"));
  }

  @Test
  void noIfRangePossible() throws IOException {
    mockWebServer.enqueue(
        new MockResponse.Builder().addHeader("Accept-Ranges", "bytes").code(200).build());
    provider.get(0, 0, null).read();
    Assertions.assertThrows(
        IOException.class,
        () -> {
          provider.get(1, 0, null).read();
        },
        "Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified");
  }

  @Test
  void weakETag() throws IOException {
    mockWebServer.enqueue(
        new MockResponse.Builder()
            .addHeader("Accept-Ranges", "bytes")
            .addHeader("ETag", "W/\"weak etag\"")
            .code(200)
            .build());
    provider.get(0, 0, null).read();
    Assertions.assertThrows(
        IOException.class,
        () -> {
          provider.get(123, 0, null).read();
        },
        "Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified");
  }

  @Test
  void noPartialResponse() throws IOException {
    mockWebServer.enqueue(
        new MockResponse.Builder()
            .addHeader("Accept-Ranges", "bytes")
            .addHeader("ETag", "\"something\"")
            .code(200)
            .build());
    mockWebServer.enqueue(new MockResponse.Builder().code(200).build());
    provider.get(0, 0, null).read();
    Assertions.assertThrows(
        RuntimeException.class,
        () -> {
          provider.get(123, 0, null).read();
        },
        "Error retrying HTTP request at position 123: expected 206 response status but got 200");
  }
}
