/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.loader;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class NHRLoaderUtils {

    public static final String BUNDLE_NAME = "NHRLoader";

    static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getUserAgent() {
        return String.format("%s, %s (%s)/%s, %s/%s",
                getBundleString("app.user-agent"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version")
        );
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static String getBundleString(String key) {
        return bundle.getString(key);
    }

    public static String getMessageFormattedString(String key, Object... args) {
        ResourceBundle bundle = getBundle();
        return new MessageFormat(bundle.getString(key), bundle.getLocale()).format(args);
    }

    public static String getLoaderVersion() {
        return getBundleString("app.version");
    }

    public static String getBrmoVersion() {
        return getBundleString("brmo.version");
    }
}
