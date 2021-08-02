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

public interface HttpClientWrapper<REQUEST,RESPONSE> {
    HttpResponseWrapper request(URI uri, String... requestHeaders) throws IOException, InterruptedException;

    default void beforeRequest(REQUEST request) {
    }

    HttpResponseWrapper wrapResponse(RESPONSE response);
}
