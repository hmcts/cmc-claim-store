package uk.gov.hmcts.cmc.domain.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeFactory {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private LocalDateTimeFactory() {}

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

}
