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

/**
 * Provides default wrapper implementations for Java HTTP clients.
 * <p>
 * If the {@code java.specification.version} system property is 11 or higher, returns a {@link Java11HttpClientWrapper}
 * unless the system property {@code httpclientwrapper} is set to {@code urlconnection}. This client tries to upgrade
 * the connection HTTP/2 by default, unless the {@code httpclientwrapper.java11.http1_1} system property is set.
 * <p>
 * When the Java version is lower than 11, a {@code URLConnectionHttpClientWrapper} is returned.
 *
 * @author Matthijs Laan
 */
public class HttpClientWrappers {
    public static HttpClientWrapper getDefault() {
        if ("urlconnection".equals(System.getProperty("httpclientwrapper"))) {
            return new URLConnectionHttpClientWrapper();
        }
        Float f = Float.parseFloat(System.getProperty(("java.specification.version")));
        if (f != null && f >= 11.0f) {
            return new Java11HttpClientWrapper();
        } else {
            return new URLConnectionHttpClientWrapper();
        }
    }
}
