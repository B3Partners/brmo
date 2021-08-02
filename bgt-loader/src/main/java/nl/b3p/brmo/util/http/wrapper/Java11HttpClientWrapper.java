/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http.wrapper;

import nl.b3p.brmo.util.http.HttpClientWrapper;
import nl.b3p.brmo.util.http.HttpResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Wraps the Java 11 {@link java.net.http.HttpClient}. Follows redirects by default but sets the HTTP version to 1.1
 * instead of HTTP/2 if the {@code httpclientwrapper.java11.http1_1} system property is set.
 *
 * @author Matthijs Laan
 */
public class Java11HttpClientWrapper implements HttpClientWrapper<HttpRequest.Builder,HttpResponse<InputStream>> {
    private final HttpClient httpClient;

    public Java11HttpClientWrapper() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL));
    }

    public Java11HttpClientWrapper(HttpClient.Builder builder) {
        // Check system property if we must disable HTTP/2 if a server gives problems (such as
        // https://npmjs.org/package/lite-server)
        if (System.getProperty("httpclientwrapper.java11.http1_1") != null) {
            builder.version(HttpClient.Version.HTTP_1_1);
        }
        this.httpClient = builder.build();
    }

    @Override
    public HttpResponseWrapper request(URI uri, String... requestHeaders) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
        if (requestHeaders.length > 0) {
            requestBuilder.headers(requestHeaders);
        }
        beforeRequest(requestBuilder);
        HttpRequest request = requestBuilder.build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return wrapResponse(response);
    }

    /**
     * Called before doing a request, override to modify or log it.
     * @param requestBuilder The request that will be done, can be modified
     */
    public void beforeRequest(HttpRequest.Builder requestBuilder) {
    }

    /**
     * Wraps a HttpResponse, override to modify or log it.
     * @param response The received response to be wrapped
     * @return The wrapped response
     */
    public HttpResponseWrapper wrapResponse(HttpResponse<InputStream> response) {
        return new HttpResponseWrapper() {
            @Override
            public int getStatusCode() {
                return response.statusCode();
            }

            @Override
            public String getHeader(String header) {
                return response.headers().firstValue(header).orElse(null);
            }

            @Override
            public InputStream getResponseBody() {
                return response.body();
            }
        };
    }
}
