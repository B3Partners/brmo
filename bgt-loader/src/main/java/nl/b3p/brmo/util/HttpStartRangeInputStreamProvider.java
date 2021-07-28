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
    private boolean first = true;
    private String ifRange;
    private String acceptRanges;

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

        if (position > 0 && !first) {
            if (!"bytes".equals(acceptRanges)) {
                throw new IOException("Exception reading from HTTP server and resume not supported", causeForRetry);
            }
            if (ifRange == null) {
                throw new IOException("Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified", causeForRetry);
            }
            requestBuilder.headers("If-Range", ifRange);
        }
        if (position > 0) {
            requestBuilder.headers("Range", "bytes=" + position + "-");
        }
        first = false;
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
        if (eTag.isPresent()) {
            String eTagValue = eTag.get();
            // Use strong ETag only
            if (!eTagValue.startsWith("W/") && eTagValue.startsWith("\"") && eTagValue.charAt(eTagValue.length()-1) == '"') {
                ifRange = eTagValue.substring(1, eTagValue.length()-1);
            }
        } else {
            ifRange = lastModified.orElse(null);
        }
        acceptRanges = response.headers().firstValue("Accept-Ranges").orElse(null);

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
