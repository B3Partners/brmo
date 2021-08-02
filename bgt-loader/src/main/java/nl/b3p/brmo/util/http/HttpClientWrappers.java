/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import nl.b3p.brmo.util.http.wrapper.Java11HttpClientWrapper;
import nl.b3p.brmo.util.http.wrapper.URLConnectionHttpClientWrapper;

import java.net.http.HttpClient;

public class HttpClientWrappers {
    public static HttpClientWrapper getDefault() {
        if ("urlconnection".equals(System.getProperty("httpclientwrapper"))) {
            return new URLConnectionHttpClientWrapper();
        }
        Float f = Float.parseFloat(System.getProperty(("java.specification.version")));
        if (f != null && f >= 11.0f) {
            if (System.getProperty("httpclientwrapper.http1_1") != null) {
                return new Java11HttpClientWrapper(HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .version(HttpClient.Version.HTTP_1_1)
                        .build());
            } else {
                return new Java11HttpClientWrapper();
            }
        } else {
            return new URLConnectionHttpClientWrapper();
        }
    }
}
