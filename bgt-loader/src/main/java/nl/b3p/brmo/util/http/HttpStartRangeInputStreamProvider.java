/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import nl.b3p.brmo.util.ResumingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides a stream reading a HTTP entity starting at a specified position until the end of the entity using
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range">HTTP Range requests</a>.
 * <p>
 * For any requests after the first, an
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-Range">If-Range</a> header is sent to guarantee
 * that the stored resource has not been modified since the last fragment has been received.
 * <p>
 * If the HTTP server does not answer range requests with the "206 Partial Content" response an error is thrown.
 *
 * @author Matthijs Laan
 */
public class HttpStartRangeInputStreamProvider implements ResumingInputStream.StreamAtStartPositionProvider {

    private final URI uri;
    private final HttpClientWrapper httpClientWrapper;

    private boolean first = true;
    private String ifRange;
    private String acceptRanges;

    public HttpStartRangeInputStreamProvider(URI uri) {
        this(uri, HttpClientWrappers.getDefault());
    }

    public HttpStartRangeInputStreamProvider(URI uri, HttpClientWrapper httpClientWrapper) {
        this.uri = uri;
        this.httpClientWrapper = httpClientWrapper;
    }

    @Override
    public InputStream get(long position, int totalRetries, Exception causeForRetry) throws IOException {
        List<String[]> headers = new ArrayList<>();

        if (position > 0 && !first) {
            if (!"bytes".equals(acceptRanges)) {
                throw new IOException("Exception reading from HTTP server and resume not supported", causeForRetry);
            }
            if (ifRange == null) {
                throw new IOException("Exception reading from HTTP server, cannot resume HTTP request reliably: no strong ETag or Last-Modified", causeForRetry);
            }
            headers.add(new String[] {"If-Range", ifRange});
        }
        if (position > 0) {
            headers.add(new String[] {"Range", "bytes=" + position + "-"});
        }
        first = false;

        HttpResponseWrapper response;
        try {
            response = httpClientWrapper.request(uri, headers.stream().flatMap(header -> Stream.of(header[0], header[1])).toArray(String[]::new));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (position > 0) {
            if(response.getStatusCode() != 206) {
                throw new RuntimeException("Error retrying HTTP request at position " + position + ": expected 206 response status but got " + response.getStatusCode());
            }
        } else if (response.getStatusCode() != 200) {
            throw new RuntimeException("HTTP status code: " + response.getStatusCode());
        }
        String lastModified = response.getHeader("Last-Modified");
        String eTag = response.getHeader("ETag");
        if (eTag != null) {
            // Use strong ETag only
            if (!eTag.startsWith("W/") && eTag.startsWith("\"") && eTag.charAt(eTag.length()-1) == '"') {
                ifRange = eTag.substring(1, eTag.length()-1);
            }
        } else {
            ifRange = lastModified;
        }
        acceptRanges = response.getHeader("Accept-Ranges");

        return response.getResponseBody();
    }
}
