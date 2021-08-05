/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A wrapper around a HTTP client with only the functionality needed by this package.
 * @param <REQUEST> An implementation defined HTTP request type.
 * @param <RESPONSE> An implementation defined HTTP response type.
 *
 * @author Matthijs Laan
 */
public interface HttpClientWrapper<REQUEST,RESPONSE> {
    /**
     * Perform a synchronous HTTP GET request.
     * @param uri The URI.
     * @param requestHeaders The request headers to send. The supplied String instances must alternate as header names
     *                       and header values. Only single request header values are expected.
     * @return A wrapper with the response.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException
     */
    HttpResponseWrapper request(URI uri, String... requestHeaders) throws IOException, InterruptedException;

    /**
     * Override to modify the request before it is sent, such as adding a {@code User-Agent} header.
     * @param request The implementation defined HTTP request type.
     */
    default void beforeRequest(REQUEST request) {
    }

    /**
     * Wrap the HTTP response so the status code, response headers en the response body can be read.
     * @param response The implementation defined HTTP response type.
     * @return
     */
    HttpResponseWrapper wrapResponse(RESPONSE response);
}
