package nl.b3p.brmo.imgeo.cli;

import java.time.Duration;
import java.time.Instant;

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
}
