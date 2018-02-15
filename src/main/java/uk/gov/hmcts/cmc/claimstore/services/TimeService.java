package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeService {
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    public LocalDateTime fromUTC(LocalDateTime input) {
        return input.atZone(UTC_ZONE)
            .withZoneSameInstant(LOCAL_ZONE)
            .toLocalDateTime();
    }

    public LocalDateTime nowInLocalZone() {
        return LocalDateTime.now(LOCAL_ZONE);
    }
}
