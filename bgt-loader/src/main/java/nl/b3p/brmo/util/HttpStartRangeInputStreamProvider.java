/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.Consumer;

public class HttpStartRangeInputStreamProvider implements ResumableInputStream.StreamAtStartPositionProvider {

    private final URI uri;
    private final HttpClient httpClient;
    private final Consumer<HttpRequest.Builder> httpRequestModifier;
    private String ifRange;

    public HttpStartRangeInputStreamProvider(URI uri) {
        this(uri, HttpClient.newHttpClient());
    }

    public HttpStartRangeInputStreamProvider(URI uri, HttpClient httpClient) {
        this(uri, httpClient, null);
    }

    public HttpStartRangeInputStreamProvider(URI uri, HttpClient httpClient, Consumer<HttpRequest.Builder> httpRequestModifier) {
        this.uri = uri;
        this.httpClient = httpClient;
        this.httpRequestModifier = httpRequestModifier;
    }

    @Override
    public InputStream get(long position, int totalRetries, Exception causeForRetry) throws IOException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
        if (httpRequestModifier != null) {
            httpRequestModifier.accept(requestBuilder);
        }

        if (position > 0) {
            if (ifRange == null) {
                throw new IOException("Cannot resume HTTP request reliably; no ETag or Last-Modified");
            }
            requestBuilder.headers("Range", "bytes=" + position + "-");
            requestBuilder.headers("If-Range", ifRange);
        }
        HttpRequest request = requestBuilder.build();

        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (position > 0) {
            if(response.statusCode() != 206) {
                throw new RuntimeException("Error retrying HTTP request at position " + position + ": expected 206 response status but got " + response.statusCode());
            }
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP status code: " + response.statusCode());
        }
        Optional<String> lastModified = response.headers().firstValue("Last-Modified");
        Optional<String> eTag = response.headers().firstValue("ETag");
        ifRange = eTag.orElseGet(() -> lastModified.orElse(null));

        afterHttpRequest(response);

        return response.body();
    }

    /**
     * Override to get access to the HttpResponse gotten from a successful request to get an input stream at a start
     * position.
     * @param response The response
     */
    public void afterHttpRequest(HttpResponse<InputStream> response) {
    }
}
