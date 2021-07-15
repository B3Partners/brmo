package nl.b3p.brmo.bgt.loader.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

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

    public static HttpResponse<Void> getHEADResponse(URI URI) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI)
                .method("HEAD", noBody())
                .build();
        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(String.format("HEAD for URI \"%s\" returned status code %d", URI, response.statusCode()));
        }
        return response;
    }
}
