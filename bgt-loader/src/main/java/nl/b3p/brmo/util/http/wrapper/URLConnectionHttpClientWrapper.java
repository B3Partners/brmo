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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

public class URLConnectionHttpClientWrapper implements HttpClientWrapper<HttpURLConnection,HttpURLConnection> {
    @Override
    public HttpResponseWrapper request(URI uri, String... requestHeaders) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Expected HttpURLConnection instance for URI " + uri);
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setInstanceFollowRedirects(true);
        if (requestHeaders.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        for(int i = 0; i < requestHeaders.length; i += 2) {
            httpURLConnection.setRequestProperty(requestHeaders[i], requestHeaders[i+1]);
        }

        beforeRequest(httpURLConnection);
        httpURLConnection.connect();

        return wrapResponse(httpURLConnection);
    }

    /**
     * Called before doing a request, override to modify or log it.
     * @param httpURLConnection The HttpURLConnection that will be used, can be modified
     */
    public void beforeRequest(HttpURLConnection httpURLConnection) {
    }

    /**
     * Wraps a HttpURLConnection after HttpURLConnection.connect() has been called, override to modify or log it.
     * @param httpURLConnection The URL connection to be wrapped
     * @return The wrapped response
     */
    @Override
    public HttpResponseWrapper wrapResponse(HttpURLConnection httpURLConnection) {
        return new HttpResponseWrapper() {
            @Override
            public int getStatusCode() throws IOException {
                return httpURLConnection.getResponseCode();
            }

            @Override
            public String getFirstHeader(String header) {
                return httpURLConnection.getHeaderField(header);
            }

            @Override
            public InputStream getResponseBody() throws IOException {
                return httpURLConnection.getInputStream();
            }
        };
    }
}
