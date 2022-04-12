/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.xml.leveringsdocument.BAGExtractLevering;
import nl.b3p.brmo.bag2.xml.leveringsdocument.Gemeente;
import nl.b3p.brmo.util.http.HttpSeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BAG2LoaderUtils {

    public static final String BUNDLE_NAME = "BAG2Loader";

    public static final String LEVERINGSDOCUMENT_FILENAME = "Leveringsdocument-BAG-Extract.xml";

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

    public static BAGExtractSelectie getBAGExtractSelectieFromZip(String zipFileName) throws IOException {
        if (zipFileName.startsWith("http://") || zipFileName.startsWith("https://")) {
            // Try to parse filename in URL to avoid having to download the ZIP and extract the leveringsdocument --
            // because this may happen many times to check whether a mutatie ZIP is applicable
            try {
                return BAGExtractSelectieFromFilename.parse(zipFileName);
            } catch(IllegalArgumentException iae) {
                // Could not parse BAG2 extract selection from filename, use HTTP random access to parse only
                // leveringsdocument without downloading/streaming the entire ZIP file
                return getBAGExtractSelectieFromHttpZip(URI.create(zipFileName));

            }
        } else {
            return getBAGExtractSelectieFromZipFile(new File(zipFileName));
        }
    }

    private static BAGExtractSelectie getBAGExtractSelectieFromHttpZip(URI uri) throws IOException {
        String message = getMessageFormattedString("load.leveringsdocument.readzip", uri);
        try(
                HttpSeekableByteChannel channel = new HttpSeekableByteChannel(uri);
                org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(
                        channel,
                        uri.toString(),
                        "UTF8",
                        false,
                        true
                )
        ) {
            message = getMessageFormattedString("load.leveringsdocument.zipentries", uri);
            Iterator<ZipArchiveEntry> iterator = zipFile.getEntries(LEVERINGSDOCUMENT_FILENAME).iterator();
            if (!iterator.hasNext()) {
                throw new IOException(getMessageFormattedString("load.leveringsdocument.notfound", uri));
            }
            message = getMessageFormattedString("load.leveringsdocument.unmarshal", uri);
            JAXBContext jaxbContext = JAXBContext.newInstance(BAGExtractLevering.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            try (InputStream in = zipFile.getInputStream(iterator.next())) {
                BAGExtractLevering levering = (BAGExtractLevering) unmarshaller.unmarshal(in);
                return new BAGExtractSelectieFromLeveringsdocument(levering);
            }
        } catch(Exception e) {
            throw new IOException(message, e);
        }
    }

    public static BAGExtractSelectie getBAGExtractSelectieFromZipFile(File zipFile) throws IOException {
        String zipFileName = zipFile.getName();
        String message = getMessageFormattedString("load.leveringsdocument.readzip", zipFileName);
        try (ZipFile zf = new ZipFile(zipFile)) {
            message = getMessageFormattedString("load.leveringsdocument.zipentries", zipFileName);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if(LEVERINGSDOCUMENT_FILENAME.equals(entry.getName())) {
                    message = getMessageFormattedString("load.leveringsdocument.unmarshal", zipFileName);
                    JAXBContext jaxbContext = JAXBContext.newInstance(BAGExtractLevering.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    try (InputStream in = zf.getInputStream(entry)) {
                        BAGExtractLevering levering = (BAGExtractLevering) unmarshaller.unmarshal(in);
                        return new BAGExtractSelectieFromLeveringsdocument(levering);
                    }
                }
            }
        } catch(Exception e) {
            throw new IOException(message, e);
        }
        throw new IOException(getMessageFormattedString("load.leveringsdocument.notfound", zipFileName));
    }

    public interface BAGExtractSelectie {
        boolean isStand();
        boolean isGebiedNLD();
        LocalDate getMutatiesFrom();
        LocalDate getMutatiesTot();
        Set<String> getGemeenteCodes();
    }

    public static class BAGExtractSelectieFromFilename implements BAGExtractSelectie {
        private String name;
        private boolean isStand;
        private boolean isGebiedNLD;
        private String gemeenteCode;
        private LocalDate mutatiesFrom;
        private LocalDate mutatiesTot;

        /**
         * @param name BAG2 filename to parse, can be URL or full path.
         * @throws IllegalArgumentException If the filename is not a BAG2 filename (outer zip only)
         */
        public static BAGExtractSelectie parse(String name) throws IllegalArgumentException {

            if (name.startsWith("http://") || name.startsWith("https://")) {
                URI uri = URI.create(name);
                String[] parts = uri.getPath().split("/");
                name = parts[parts.length-1];
            }
            if (name.contains(File.separator)) {
                Path p = Path.of(name);
                name = p.getFileName().toString();
            }

            BAGExtractSelectieFromFilename selectie = new BAGExtractSelectieFromFilename();
            selectie.name = name;

            if (name.equals("lvbag-extract-nl.zip") || name.matches("BAGNLDL-\\d{8}\\.zip")) {
                selectie.isStand = true;
                selectie.isGebiedNLD = true;
                return selectie;
            }

            if (name.matches("BAGGEM\\d{4}L-\\d{8}\\.zip")) {
                selectie.isStand = true;
                selectie.isGebiedNLD = false;
                selectie.gemeenteCode = name.substring(6, 10);
                return selectie;
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");

            if (name.matches("BAGGEM\\d{4}M-\\d{8}-\\d{8}\\.zip")) {
                selectie.isStand = false;
                selectie.isGebiedNLD = false;
                selectie.gemeenteCode = name.substring(6, 10);
                // Gemeente can only have monthly updates
                selectie.mutatiesFrom = LocalDate.parse(name.substring(12, 20), dtf);
                selectie.mutatiesTot = LocalDate.parse(name.substring(21, 29), dtf);
                return selectie;
            }

            if (name.matches("BAGNLDM-\\d{8}-\\d{8}\\.zip")) {
                selectie.isStand = false;
                selectie.isGebiedNLD = true;
                selectie.mutatiesFrom = LocalDate.parse(name.substring(8, 16), dtf);
                selectie.mutatiesTot = LocalDate.parse(name.substring(17, 25), dtf);
                return selectie;
            }

            throw new IllegalArgumentException("Ongeldige BAG2 bestandsnaam: " + name);
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean isStand() {
            return isStand;
        }

        @Override
        public boolean isGebiedNLD() {
            return isGebiedNLD;
        }

        @Override
        public Set<String> getGemeenteCodes() {
            return Collections.singleton(gemeenteCode);
        }

        @Override
        public LocalDate getMutatiesFrom() {
            return mutatiesFrom;
        }

        @Override
        public LocalDate getMutatiesTot() {
            return mutatiesTot;
        }
    }

    public static class BAGExtractSelectieFromLeveringsdocument implements BAGExtractSelectie {
        BAGExtractLevering levering;

        private BAGExtractSelectieFromLeveringsdocument(BAGExtractLevering levering) {
            this.levering = levering;
        }

        @Override
        public boolean isStand() {
            return levering.getSelectieGegevens().getLVCExtract() != null;
        }

        @Override
        public boolean isGebiedNLD() {
            return levering.getSelectieGegevens().getGebiedRegistratief().getGebiedNLD() != null;
        }

        @Override
        public LocalDate getMutatiesFrom() {
            XMLGregorianCalendar xmlGregorianCalendar = levering.getSelectieGegevens().getMUTExtract().getMutatieperiode().getMutatiedatumVanaf();
            return LocalDate.of(
                    xmlGregorianCalendar.getYear(),
                    xmlGregorianCalendar.getMonth(),
                    xmlGregorianCalendar.getDay());
        }

        @Override
        public LocalDate getMutatiesTot() {
            XMLGregorianCalendar xmlGregorianCalendar = levering.getSelectieGegevens().getMUTExtract().getMutatieperiode().getMutatiedatumTot();
            return LocalDate.of(
                    xmlGregorianCalendar.getYear(),
                    xmlGregorianCalendar.getMonth(),
                    xmlGregorianCalendar.getDay());
        }

        @Override
        public Set<String> getGemeenteCodes() {
            return levering.getSelectieGegevens().getGebiedRegistratief().getGebiedGEM().getGemeenteCollectie().getGemeente().stream()
                    .map(Gemeente::getGemeenteIdentificatie)
                    .collect(Collectors.toSet());
        }
    }
}
