package uk.gov.hmcts.cmc.domain.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DatesProvider {

    public static final LocalDateTime NOW_IN_LOCAL_ZONE = LocalDateTimeFactory.nowInLocalZone();
    public static final LocalDate ISSUE_DATE = NOW_IN_LOCAL_ZONE.toLocalDate().plusDays(1);
    public static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
    public static final LocalDate INTEREST_DATE = NOW_IN_LOCAL_ZONE.toLocalDate().minusDays(101);

    private DatesProvider() {
        // NO-OP
    }

    public static LocalDateTime toDateTime(String dateString) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public static LocalDate toDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
