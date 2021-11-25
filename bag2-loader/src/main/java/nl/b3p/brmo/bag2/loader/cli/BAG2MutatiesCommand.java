/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import nl.b3p.brmo.bag2.loader.BAG2Database;
import nl.b3p.brmo.bag2.loader.BAG2LoaderUtils;
import nl.b3p.brmo.bag2.loader.BAG2ProgressReporter;
import nl.b3p.brmo.util.ResumingInputStream;
import nl.b3p.brmo.util.http.HttpStartRangeInputStreamProvider;
import nl.b3p.brmo.util.http.wrapper.Java11HttpClientWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

@Command(name = "mutaties", mixinStandardHelpOptions = true)
public class BAG2MutatiesCommand {
    private static final Log log = LogFactory.getLog(BAG2MutatiesCommand.class);

    @ParentCommand
    private BAG2LoaderMain parent;

    private static final String KADASTER_LOGIN_URL = "https://mijn.kadaster.nl/security/login.do";

    // Artikelnummer 2529 is dagmutaties (https://www.kadaster.nl/-/handleiding-soap-service-bag-2.0-extract)
    private static final String LVBAG_BESTANDEN_API_URL = "https://bag.kadaster.nl/lvbag/bag-bestanden/api/bestanden";

    @Command(name="download", sortOptions = false)
    public int download(
            @Option(names="--no-delete", negatable = true) boolean noDelete,
            @Option(names="--kadaster-user") String kadasterUser,
            @Option(names="--kadaster-password") String kadasterPassword,
            @Option(names="--url", defaultValue = LVBAG_BESTANDEN_API_URL) String url,
            @Option(names="--query-params", defaultValue = "artikelnummers=2529") String queryParams,
            @Option(names="--path", defaultValue = "") String downloadPath,
            @Option(names="--mirror-base-url") String mirrorBaseUrl,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
            ) throws Exception {

        log.info(BAG2LoaderUtils.getUserAgent());
        Instant start = Instant.now();

        CookieManager kadasterCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        mijnKadasterLogin(URI.create(url), kadasterUser, kadasterPassword, kadasterCookieManager);

        JSONArray bestanden = getBagBestanden(url, queryParams, kadasterCookieManager);

        log.info("Aantal beschikbare bestanden: " + bestanden.length());

        List<JSONObject> toDownload = new ArrayList<>();
        for(int i = 0; i < bestanden.length(); i++) {
            JSONObject bestand = bestanden.getJSONObject(i);

            File f = Path.of(downloadPath, bestand.getString("naam")).toFile();
            if (!f.exists() || f.length() != bestand.getLong("grootte")) {
                toDownload.add(bestand);
            }
        }

        if (!noDelete) {
            deleteZipFilesNotInBestanden(downloadPath, bestanden);
        }

        long totalBytes = toDownload.stream().map(bestand -> bestand.getLong("grootte")).reduce(Long::sum).orElse(0L);
        log.info(String.format("Aantal te downloaden naar directory \"%s\": %d bestanden (%s)",
                Path.of(downloadPath).toAbsolutePath(),
                toDownload.size(),
                byteCountToDisplaySize(totalBytes)));

        int count = 0;
        long bytesRead = 0;
        for(JSONObject bestand: toDownload) {
            url = bestand.getString("url");
            String name = bestand.getString("naam");

            try(ResumingInputStream input = new ResumingInputStream(new HttpStartRangeInputStreamProvider(URI.create(url),
                    new Java11HttpClientWrapper(HttpClient.newBuilder().cookieHandler(kadasterCookieManager))));
                OutputStream out = new FileOutputStream(Path.of(downloadPath, name).toFile())) {
                log.info(String.format("Bestand %2d/%d (%.1f%%): downloaden %s...",
                        ++count,
                        toDownload.size(),
                        (100.0 / totalBytes) * bytesRead,
                        name));
                IOUtils.copyLarge(input, out);
            }
            bytesRead += bestand.getLong("grootte");
        }
        if (!toDownload.isEmpty()) {
            String msg = "";
            if (mirrorBaseUrl != null) {
                for (int i = 0; i < bestanden.length(); i++) {
                    JSONObject bestand = bestanden.getJSONObject(i);
                    String name = bestand.getString("naam");
                    bestand.put("url", URI.create(mirrorBaseUrl).resolve(name));
                }
                File bestandenJSONMirror = Path.of(downloadPath, "bestanden.json").toFile();
                try(OutputStream out = new FileOutputStream(bestandenJSONMirror)) {
                    IOUtils.write(bestanden.toString(2), out, StandardCharsets.UTF_8);
                    msg = String.format(", JSON voor mirror \"%s\" geschreven naar \"%s\"", mirrorBaseUrl, bestandenJSONMirror);
                }
            }
            log.info("Alle bestanden gedownload in " + formatTimeSince(start) + msg);
        }

        return ExitCode.OK;
    }

    @Command(name="apply", sortOptions = false)
    public int apply(
            @Mixin BAG2DatabaseOptions dbOptions,
            @Mixin BAG2ProgressOptions progressOptions,
            @Option(names="--kadaster-user") String kadasterUser,
            @Option(names="--kadaster-password") String kadasterPassword,
            @Option(names="--url", defaultValue = LVBAG_BESTANDEN_API_URL) String url,
            @Option(names="--query-params", defaultValue = "artikelnummers=2529") String queryParams,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp
    ) throws Exception {

        log.info(BAG2LoaderUtils.getUserAgent());
        Instant start = Instant.now();

        CookieManager kadasterCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        mijnKadasterLogin(URI.create(url), kadasterUser, kadasterPassword, kadasterCookieManager);

        JSONArray bestanden = getBagBestanden(url, queryParams, kadasterCookieManager);

        log.info("Aantal beschikbare bestanden: " + bestanden.length());

        List<String> names = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for(int i = 0; i < bestanden.length(); i++) {
            JSONObject bestand = bestanden.getJSONObject(i);
            names.add(bestand.getString("naam"));
            urls.add(bestand.getString("url"));
        }

        try(BAG2Database db = new BAG2Database(dbOptions)) {
            BAG2ProgressReporter progressReporter = progressOptions.isConsoleProgressEnabled()
                    ? new BAG2ConsoleProgressReporter()
                    : new BAG2ProgressReporter();

            parent.applyMutaties(db, dbOptions, new BAG2LoadOptions(), progressReporter, names.toArray(String[]::new), urls.toArray(String[]::new), kadasterCookieManager);
            log.info("Alle mutatiebestanden verwerkt in " + formatTimeSince(start));
            return ExitCode.OK;
        }
    }

    private static void mijnKadasterLogin(URI forUri, String username, String password, CookieManager cookieManager) throws IOException, InterruptedException {
        if (!forUri.getHost().endsWith("kadaster.nl")) {
            return;
        }
        if (username == null || password == null) {
            throw new IllegalArgumentException("Gebruikersnaam en wachtwoord zijn verplicht");
        }

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(KADASTER_LOGIN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(String.format(
                        "user=%s&password=%s",
                        URLEncoder.encode(username, StandardCharsets.UTF_8),
                        URLEncoder.encode(password, StandardCharsets.UTF_8))))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 302) {
            throw new IllegalArgumentException(String.format("Fout bij inloggen op Mijn Kadaster met gebruikersnaam \"%s\"", username));
        }

        Map<String,List<String>> cookies = cookieManager.get(URI.create(KADASTER_LOGIN_URL), new HashMap<>());

        Optional<String> kadasterTicketIdCookie = cookies.getOrDefault("Cookie", List.of())
                .stream().filter(c -> c.startsWith("KadasterTicketId=")).findFirst();

        if (kadasterTicketIdCookie.isEmpty()) {
            throw new IllegalArgumentException("Geen KadasterTicketId cookie ontvangen na inloggen");
        }
    }

    private static JSONArray getBagBestanden(String url, String queryParams, CookieManager kadasterCookieManager) throws Exception {
        url = url + "?" + queryParams;
        log.info("Opvragen bestanden JSON vanaf URL " + url);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(kadasterCookieManager)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray bestanden;
        try {
            bestanden = new JSONArray(response.body());
        } catch(Exception e) {
            throw new Exception("Fout bij parsen BAG bestanden JSON, body: " + response.body(), e);
        }
        return bestanden;
    }

    private static void deleteZipFilesNotInBestanden(String downloadPath, JSONArray bestanden) throws IOException {
        final Set<String> names = new HashSet<>();
        for(int i = 0; i < bestanden.length(); i++) {
            names.add(bestanden.getJSONObject(i).getString("naam"));
        }
        try(Stream<Path> stream = Files.list(Path.of(downloadPath))) {
            stream.filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".zip") && !names.contains(p.getFileName().toString()))
                    .forEach(p -> {
                        log.info("Verwijderen ZIP bestand niet in bestandenlijst: " + p.getFileName());
                        try {
                            Files.delete(p);
                        } catch(Exception e) {
                            log.error(String.format("Fout bij verwijderen ZIP bestand %s: %s", p.getFileName(), e));
                        }
                    });
        }
    }
}
