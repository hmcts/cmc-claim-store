package uk.gov.hmcts.cmc.claimstore.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.util.Objects.requireNonNull;

public class DateUtils {
    public static LocalDateTime startOfDay(LocalDate localDate) {
        requireNonNull(localDate);

        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }

    public static LocalDateTime endOfDay(LocalDate localDate) {
        requireNonNull(localDate);

        return LocalDateTime.of(localDate, LocalTime.MAX);
    }
}
