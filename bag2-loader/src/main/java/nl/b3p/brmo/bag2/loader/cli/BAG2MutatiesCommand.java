/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine.*;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Command(name = "mutaties", mixinStandardHelpOptions = true)
public class BAG2MutatiesCommand {
    private static final Log log = LogFactory.getLog(BAG2MutatiesCommand.class);

    private static final String KADASTER_LOGIN_URL = "https://mijn.kadaster.nl/security/login.do";

    // Artikelnummer 2529 is dagmutaties (https://www.kadaster.nl/-/handleiding-soap-service-bag-2.0-extract)
    private static final String LVBAG_BESTANDEN_API_URL = "https://bag.kadaster.nl/lvbag/bag-bestanden/api/bestanden?artikelnummers=&gemeenteCodes=";

    @Command(name="download", sortOptions = false)
    public int download(
            @Option(names="--no-delete", negatable = true) boolean noDelete,
            @Option(names="--kadaster-user") String kadasterUser,
            @Option(names="--kadaster-password") String kadasterPassword,
            @Option(names="--url", defaultValue = LVBAG_BESTANDEN_API_URL) String url
            ) throws IOException, InterruptedException {
        System.out.printf("mutaties download, nodelete=%s, kad user=%s, kad pass=%s, url=%s\n", noDelete, kadasterUser, kadasterPassword, url);

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
        log.info("Auth cookie: " + kadasterTicketIdCookie.get());

        request = HttpRequest.newBuilder().uri(URI.create(LVBAG_BESTANDEN_API_URL + "?artikelnummers")).build();
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.printf("Bestanden response status %d, body: %s\n", response.statusCode(), response.body());

        return ExitCode.OK;
    }
}
