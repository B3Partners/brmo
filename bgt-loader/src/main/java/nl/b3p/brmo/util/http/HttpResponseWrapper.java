/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper around a HTTP response to provide access to the response status, headers and body.
 *
 * @author Matthijs Laan
 */
public interface HttpResponseWrapper {
    /**
     * @return The response status code.
     * @throws IOException
     */
    int getStatusCode() throws IOException;

    /**
     * @param header The name of the header value to return. Only single header values are expected.
     * @return
     */
    String getHeader(String header);

    /**
     * The input stream for the response body. Closing this stream should release all resources for this request.
     */
    InputStream getResponseBody() throws IOException;
}
