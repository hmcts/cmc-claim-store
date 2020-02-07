package uk.gov.hmcts.cmc.domain.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LocalDateTimeFactory {

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private LocalDateTimeFactory() {
    }

    public static LocalDateTime fromNullableUTCtoLocalZone(LocalDateTime input) {
        return input == null ? null : fromUTC(input);
    }

    public static LocalDateTime fromUTC(LocalDateTime input) {
        return input.atZone(UTC_ZONE)
            .withZoneSameInstant(LOCAL_ZONE)
            .toLocalDateTime();
    }

    public static LocalDateTime nowInLocalZone() {
        return LocalDateTime.now(LOCAL_ZONE);
    }

    public static LocalDateTime nowInUTC() {
        return LocalDateTime.now(UTC_ZONE);
    }

    public static LocalDate fromLong(Long input) {
        Date date = new Date(input);
        return Instant.ofEpochMilli(date.getTime())
            .atZone(UTC_ZONE)
            .toLocalDate();
    }

}
