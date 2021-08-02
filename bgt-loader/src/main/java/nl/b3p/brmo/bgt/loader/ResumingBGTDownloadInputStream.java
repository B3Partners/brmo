/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.ConsoleProgressReporter;
import nl.b3p.brmo.bgt.schema.BGTObjectTableWriter;
import nl.b3p.brmo.util.http.HttpResponseWrapper;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.wrapper.Java11HttpClientWrapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static nl.b3p.brmo.bgt.loader.Utils.getMessageFormattedString;
import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;

public class ResumingBGTDownloadInputStream extends ResumingInputStream {
    private static final Log log = LogFactory.getLog(ResumingBGTDownloadInputStream.class);

    public ResumingBGTDownloadInputStream(URI uri, BGTObjectTableWriter writer) {
        this(uri, writer, false);
    }

    public ResumingBGTDownloadInputStream(URI uri, BGTObjectTableWriter writer, boolean logRetries) {
        super(new HttpStartRangeInputStreamProvider(uri, new Java11HttpClientWrapper() {
            @Override
            public void beforeRequest(HttpRequest.Builder requestBuilder) {
                requestBuilder.headers("User-Agent", getUserAgent());
            }

            @Override
            public HttpResponseWrapper wrapResponse(HttpResponse<InputStream> response) {
                HttpResponseWrapper wrapper = super.wrapResponse(response);

                // The direct download https://api.pdok.nl/lv/bgt/download/v1_0/full/predefined/bgt-citygml-nl-nopbp.zip
                // does not support the HEAD method to read the Content-Length -- it first sends a redirect. Read the
                // Content-Length later:

                // Only read Content-Length from the first response starting at position 0, not a response from a
                // retried request starting at a later position (we could parse the Content-Range response though).
                if (wrapper.getHeader("Content-Range") == null) {
                    String contentLength = wrapper.getHeader("Content-Range");
                    if (contentLength != null) {
                        ((ProgressReporter) writer.getProgressUpdater()).setTotalBytes(Long.parseLong(contentLength));
                    }
                }
                return wrapper;
            }
        }) {
            @Override
            public InputStream get(long position, int totalRetries, Exception causeForRetry) throws IOException {
                if (causeForRetry != null && logRetries) {
                    String msg = getMessageFormattedString("download.retry",
                            totalRetries,
                            position,
                            ExceptionUtils.getRootCause(causeForRetry).getMessage()
                    );
                    if (writer.getProgressUpdater() instanceof ConsoleProgressReporter) {
                        System.out.println("\r" + msg);
                    } else {
                        log.warn(msg);
                        log.trace(causeForRetry);
                    }
                }
                return super.get(position, totalRetries, causeForRetry);
            }
        });
    }
}
