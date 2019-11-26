package uk.gov.hmcts.cmc.claimstore.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.util.Objects.requireNonNull;

public class DateUtils {
    public static final LocalDateTime DATE_OF_5_0_0_RELEASE = LocalDateTime.of(2019, Month.SEPTEMBER, 9, 15, 12);

    private DateUtils() {
        // Utility class, no instances
    }

    public static LocalDateTime startOfDay(LocalDate localDate) {
        requireNonNull(localDate);

        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }

    public static LocalDateTime endOfDay(LocalDate localDate) {
        requireNonNull(localDate);

        return LocalDateTime.of(localDate, LocalTime.MAX);
    }

    public static String toISOFullStyle(LocalDate localDate) {
        requireNonNull(localDate);
        return localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
    }
}
