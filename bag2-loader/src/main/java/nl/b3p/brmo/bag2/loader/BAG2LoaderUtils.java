/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.ResourceBundle;

public class BAG2LoaderUtils {

    public static final String BUNDLE_NAME = "BAG2Loader";

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

    public static class BAG2FileName {
        private String name;
        private boolean isStand;
        private boolean isGemeente;
        private boolean isMaandmutaties;
        private String gemeenteCode;
        private LocalDate mutatiesFrom;
        private LocalDate mutatiesTo;


        public BAG2FileName(String name, boolean isStand, boolean isGemeente, boolean isMaandmutaties) {
            this(name, isStand, isGemeente, isMaandmutaties, null, null, null);
        }

        public BAG2FileName(String name, boolean isStand, boolean isGemeente, boolean isMaandmutaties, String gemeenteCode) {
            this(name, isStand, isGemeente, isMaandmutaties, gemeenteCode, null, null);
        }

        public BAG2FileName(String name, boolean isStand, boolean isGemeente, boolean isMaandmutaties, String gemeenteCode, LocalDate mutatiesFrom, LocalDate mutatiesTo) {
            this.name = name;
            this.isStand = isStand;
            this.isGemeente = isGemeente;
            this.isMaandmutaties = isMaandmutaties;
            this.gemeenteCode = gemeenteCode;
            this.mutatiesFrom = mutatiesFrom;
            this.mutatiesTo = mutatiesTo;
        }

        public String getName() {
            return name;
        }

        public boolean isStand() {
            return isStand;
        }

        public boolean isGemeente() {
            return isGemeente;
        }

        public boolean isMaandmutaties() {
            return isMaandmutaties;
        }

        public String getGemeenteCode() {
            return gemeenteCode;
        }

        public LocalDate getMutatiesFrom() {
            return mutatiesFrom;
        }

        public LocalDate getMutatiesTo() {
            return mutatiesTo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BAG2FileName that = (BAG2FileName) o;
            return isStand == that.isStand && isGemeente == that.isGemeente && isMaandmutaties == that.isMaandmutaties && name.equals(that.name) && Objects.equals(gemeenteCode, that.gemeenteCode) && Objects.equals(mutatiesFrom, that.mutatiesFrom) && Objects.equals(mutatiesTo, that.mutatiesTo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, isStand, isGemeente, isMaandmutaties, gemeenteCode, mutatiesFrom, mutatiesTo);
        }

        @Override
        public String toString() {
            return "BAG2FileName{" +
                    "name='" + name + '\'' +
                    ", isStand=" + isStand +
                    ", isGemeente=" + isGemeente +
                    ", isMaandmutaties=" + isMaandmutaties +
                    ", gemeenteCode='" + gemeenteCode + '\'' +
                    ", mutatiesFrom=" + mutatiesFrom +
                    ", mutatiesTo=" + mutatiesTo +
                    '}';
        }
    }

    /**
     *
     * @param name BAG2 filename to analyze, can be URL or full path.
     * @throws IllegalArgumentException If the filename is not a BAG2 filename (outer zip only)
     */
    public static BAG2FileName analyzeBAG2FileName(String name) throws IllegalArgumentException {
        if (name.startsWith("http://") || name.startsWith("https://")) {
            URI uri = URI.create(name);
            String[] parts = uri.getPath().split("/");
            name = parts[parts.length-1];
        }
        if (name.contains(File.separator)) {
            Path p = Path.of(name);
            name = p.getFileName().toString();
        }

        if (name.equals("lvbag-extract-nl.zip") || name.matches("BAGNLDL-\\d{8}\\.zip")) {
            return new BAG2FileName(name, true, false, false);
        }

        if (name.matches("BAGGEM\\d{4}L-\\d{8}\\.zip")) {
            return new BAG2FileName(name, true, true, false, name.substring(6, 10));
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");

        if (name.matches("BAGGEM\\d{4}M-\\d{8}-\\d{8}\\.zip")) {
            // Gemeente can only have monthly updates
            LocalDate mutatiesFrom = LocalDate.parse(name.substring(12, 20), dtf);
            LocalDate mutatiesTo = LocalDate.parse(name.substring(21, 29), dtf);
            return new BAG2FileName(name, false, true, true, name.substring(6, 10), mutatiesFrom, mutatiesTo);
        }

        if (name.matches("BAGNLDM-\\d{8}-\\d{8}\\.zip")) {
            // Parse dates to determine whether updates are daily or monthly
            LocalDate mutatiesFrom = LocalDate.parse(name.substring(8, 16), dtf);
            LocalDate mutatiesTo = LocalDate.parse(name.substring(17, 25), dtf);
            long days = ChronoUnit.DAYS.between(mutatiesFrom, mutatiesTo);
            return new BAG2FileName(name, false, false, days > 1, null, mutatiesFrom, mutatiesTo);
        }

        throw new IllegalArgumentException("Ongeldige BAG2 bestandsnaam: " + name);
    }
}
