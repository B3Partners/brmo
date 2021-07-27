/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.ConsoleProgressReporter;
import nl.b3p.brmo.util.HttpStartRangeInputStreamProvider;
import nl.b3p.brmo.util.ResumableInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.OptionalLong;

import static nl.b3p.brmo.bgt.loader.Utils.getUserAgent;

public class ResumableBGTDownloadInputStream extends ResumableInputStream {
    private static final Log log = LogFactory.getLog(ResumableBGTDownloadInputStream.class);

    public ResumableBGTDownloadInputStream(URI uri, BGTObjectTableWriter writer) {
        super(new HttpStartRangeInputStreamProvider(uri,
                HttpClient.newBuilder()
                        .proxy(ProxySelector.getDefault())
                        .followRedirects(HttpClient.Redirect.NORMAL).build(),
                (requestBuilder) -> requestBuilder.headers("User-Agent", getUserAgent())
        ) {
            @Override
            public InputStream get(long position, int totalRetries, Exception causeForRetry) throws IOException {
                if (causeForRetry != null) {
                    String msg = String.format("Exception reading from server, retrying (total retries: %d) from position %d. Error: %s",
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

            @Override
            public void afterHttpRequest(HttpResponse<InputStream> response) {
                // The direct download https://api.pdok.nl/lv/bgt/download/v1_0/full/predefined/bgt-citygml-nl-nopbp.zip
                // does not support the HEAD method to read the content-length because it first sends a redirect. Read
                // the Content-Length later:

                // Only read Content-Length from the first response, not a response from a retried request starting at
                // a later position
                if (!response.headers().map().containsKey("Content-Range")) {
                    OptionalLong contentLength = response.headers().firstValueAsLong("Content-Length");
                    if (contentLength.isPresent()) {
                        ((ProgressReporter) writer.getProgressUpdater()).setTotalBytes(contentLength.getAsLong());
                    }
                }
            }
        });
    }
}
