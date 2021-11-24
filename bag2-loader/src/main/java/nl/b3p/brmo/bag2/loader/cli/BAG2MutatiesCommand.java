/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

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
import picocli.CommandLine.Option;

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
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FileUtils.streamFiles;

@Command(name = "mutaties", mixinStandardHelpOptions = true)
public class BAG2MutatiesCommand {
    private static final Log log = LogFactory.getLog(BAG2MutatiesCommand.class);

    private static final String KADASTER_LOGIN_URL = "https://mijn.kadaster.nl/security/login.do";

    // Artikelnummer 2529 is dagmutaties (https://www.kadaster.nl/-/handleiding-soap-service-bag-2.0-extract)
    private static final String LVBAG_BESTANDEN_API_URL = "https://bag.kadaster.nl/lvbag/bag-bestanden/api/bestanden?";

    @Command(name="download", sortOptions = false)
    public int download(
            @Option(names="--no-delete", negatable = true) boolean noDelete,
            @Option(names="--kadaster-user") String kadasterUser,
            @Option(names="--kadaster-password") String kadasterPassword,
            @Option(names="--url", defaultValue = LVBAG_BESTANDEN_API_URL) String url,
            @Option(names="--query-params", defaultValue = "artikelnummers=2529") String queryParams,
            @Option(names="--path", defaultValue = "") String downloadPath
            ) throws IOException, InterruptedException {

        Instant start = Instant.now();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(KADASTER_LOGIN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(String.format(
                        "user=%s&password=%s",
                        URLEncoder.encode(kadasterUser, StandardCharsets.UTF_8),
                        URLEncoder.encode(kadasterPassword, StandardCharsets.UTF_8))))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 302) {
            log.error(String.format("Error logging in to Mijn Kadaster with user \"%s\"\n", kadasterUser));
            return ExitCode.SOFTWARE;
        }

        Map<String,List<String>> cookies = httpClient.cookieHandler().get().get(URI.create(LVBAG_BESTANDEN_API_URL), new HashMap<>());

        Optional<String> kadasterTicketIdCookie = cookies.getOrDefault("Cookie", (List<String>)Collections.EMPTY_LIST)
                .stream().filter(c -> c.startsWith("KadasterTicketId=")).findFirst();

        if (kadasterTicketIdCookie.isEmpty()) {
            log.error("Did not receive KadasterTicketId cookie in response");
            return ExitCode.SOFTWARE;
        }

        url = url + queryParams;
        log.info("Opvragen afgiftes vanaf URL " + url);
        request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray afgiftes = new JSONArray(response.body());
        log.info("Aantal afgiftes: " + afgiftes.length());

        List<JSONObject> toDownload = new ArrayList<>();
        for(int i = 0; i < afgiftes.length(); i++) {
            JSONObject afgifte = afgiftes.getJSONObject(i);

            File f = Path.of(downloadPath, afgifte.getString("naam")).toFile();
            if (!f.exists() || f.length() != afgifte.getLong("grootte")) {
                toDownload.add(afgifte);
            }
        }

        if (!noDelete) {
            deleteZipFilesNotInAfgiftes(downloadPath, afgiftes);
        }

        long totalBytes = toDownload.stream().map(afgifte -> afgifte.getLong("grootte")).reduce(Long::sum).orElse(0L);
        log.info(String.format("Aantal te downloaden naar directory \"%s\": %d afgiftes (%s)",
                Path.of("").toAbsolutePath(),
                toDownload.size(),
                byteCountToDisplaySize(totalBytes)));

        int count = 0;
        long bytesRead = 0;
        for(JSONObject afgifte: toDownload) {
            url = afgifte.getString("url");
            String name = afgifte.getString("naam");

            ResumingInputStream input = new ResumingInputStream(new HttpStartRangeInputStreamProvider(URI.create(url),
                    new Java11HttpClientWrapper(HttpClient.newBuilder().cookieHandler(httpClient.cookieHandler().get()))));

            log.info(String.format("Afgifte %2d/%d (%.1f%%): downloaden %s...",
                    ++count,
                    toDownload.size(),
                    (100.0 / totalBytes) * bytesRead,
                    name));
            try(OutputStream out = new FileOutputStream(Path.of(downloadPath, name).toFile())) {
                IOUtils.copy(input, out);
            }
            bytesRead += afgifte.getLong("grootte");
        }
        if (!toDownload.isEmpty()) {
            log.info("Alle afgiftes gedownload in " + formatTimeSince(start));
        }

        return ExitCode.OK;
    }

    private static void deleteZipFilesNotInAfgiftes(String downloadPath, JSONArray afgiftes) throws IOException {
        final Set<String> names = new HashSet<>();
        for(int i = 0; i < afgiftes.length(); i++) {
            names.add(afgiftes.getJSONObject(i).getString("naam"));
        }
        try(Stream<Path> stream = Files.list(Path.of(downloadPath))) {
            stream.filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".zip") && !names.contains(p.getFileName().toString()))
                    .forEach(p -> {
                        log.info("Verwijderen ZIP bestand niet in afgiftelijst: " + p.getFileName());
                        try {
                            Files.delete(p);
                        } catch(Exception e) {
                            log.error(String.format("Fout bij verwijderen ZIP bestand %s: %s", p.getFileName(), e));
                        }
                    });
        }
    }
}
