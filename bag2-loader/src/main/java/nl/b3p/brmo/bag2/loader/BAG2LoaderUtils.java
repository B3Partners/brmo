/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.xml.leveringsdocument.BAGExtractLevering;
import nl.b3p.brmo.bag2.xml.leveringsdocument.GebiedNLD;
import nl.b3p.brmo.bag2.xml.leveringsdocument.GebiedRegistratief;
import nl.b3p.brmo.bag2.xml.leveringsdocument.Gemeente;
import nl.b3p.brmo.bag2.xml.leveringsdocument.LVCExtract;
import nl.b3p.brmo.bag2.xml.leveringsdocument.MUTExtract;
import nl.b3p.brmo.bag2.xml.leveringsdocument.SelectieGegevens;
import nl.b3p.brmo.util.CountingSeekableByteChannel;
import nl.b3p.brmo.util.http.HttpSeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.ResourceBundle;
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

    public static BAGExtractLeveringWrapper findAndParseLeveringsdocumentInZip(String zipFileName) throws IOException {

        if (zipFileName.startsWith("http://") || zipFileName.startsWith("https://")) {
            // Use HTTP random access to parse only leveringsdocument without downloading/streaming the entire ZIP file
            return findAndParseLeveringsdocumentInHttpZip(URI.create(zipFileName));
        } else {
            return findAndParseLeveringsdocumentInZip(new File(zipFileName));
        }
    }

    public static BAGExtractLeveringWrapper findAndParseLeveringsdocumentInHttpZip(URI uri) throws IOException {
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
                return new BAGExtractLeveringWrapper(levering);
            }
        } catch(Exception e) {
            throw new IOException(message, e);
        }
    }

    public static BAGExtractLeveringWrapper findAndParseLeveringsdocumentInZip(File zipFile) throws IOException {
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
                        return new BAGExtractLeveringWrapper(levering);
                    }
                }
            }
        } catch(Exception e) {
            throw new IOException(message, e);
        }
        throw new IOException(getMessageFormattedString("load.leveringsdocument.notfound", zipFileName));
    }

    public static class BAGExtractLeveringWrapper extends SelectieGegevens {
        BAGExtractLevering levering;

        private BAGExtractLeveringWrapper(BAGExtractLevering levering) {
            this.levering = levering;
        }

        @Override
        public LVCExtract getLVCExtract() {
            return levering.getSelectieGegevens().getLVCExtract();
        }

        @Override
        public MUTExtract getMUTExtract() {
            return levering.getSelectieGegevens().getMUTExtract();
        }

        @Override
        public GebiedRegistratief getGebiedRegistratief() {
            return levering.getSelectieGegevens().getGebiedRegistratief();
        }

        public boolean isStand() {
            return getLVCExtract() != null;
        }

        public boolean isMutaties() {
            return getMUTExtract() != null;
        }

        public boolean isGebiedNLD() {
            return getGebiedRegistratief().getGebiedNLD() != null;
        }

        public boolean isGemeente() {
            return getGebiedRegistratief().getGebiedGEM() != null && !getGebiedRegistratief().getGebiedGEM().getGemeenteCollectie().getGemeente().isEmpty();
        }

        public LocalDate getMutatiesFrom() {
            XMLGregorianCalendar xmlGregorianCalendar = getMUTExtract().getMutatieperiode().getMutatiedatumVanaf();
            return LocalDate.of(
                    xmlGregorianCalendar.getYear(),
                    xmlGregorianCalendar.getMonth(),
                    xmlGregorianCalendar.getDay());
        }

        public String getGemeenteCodes() {
            return getGebiedRegistratief().getGebiedGEM().getGemeenteCollectie().getGemeente().stream()
                    .map(Gemeente::getGemeenteIdentificatie)
                    .collect(Collectors.joining(","));
        }
    }
}
