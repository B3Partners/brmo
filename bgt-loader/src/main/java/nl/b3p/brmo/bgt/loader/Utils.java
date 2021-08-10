/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ResourceBundle;

public class Utils {

    public static String formatTimeSince(Instant start) {
        Duration d = Duration.between(start, Instant.now());
        String days = d.toDaysPart() > 0 ? d.toDaysPart() + "d " : "";
        if (d.toHoursPart() == 0 && d.toMinutesPart() == 0) {
            return days + d.toSecondsPart() + "s";
        } else if(d.toHoursPart() == 0) {
            return String.format("%s%dm %2ds", days, d.toMinutesPart(), d.toSecondsPart());
        }
        return String.format("%s%dh %2dm %2ds", days, d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart());
    }

    public static final String BUNDLE_NAME = "BGTLoader";

    private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

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
}
