/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import java.io.IOException;
import java.io.InputStream;

public interface HttpResponseWrapper {
    int getStatusCode() throws IOException;

    String getFirstHeader(String header);

    /**
     * The input stream for the response body. Closing this stream should release all resources for this request.
     */
    InputStream getResponseBody() throws IOException;
}
