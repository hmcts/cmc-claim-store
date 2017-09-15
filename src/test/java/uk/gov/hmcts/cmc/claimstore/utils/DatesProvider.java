package uk.gov.hmcts.cmc.claimstore.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DatesProvider {

    private DatesProvider() {
        // NO-OP
    }

    public static final LocalDateTime NOW_IN_LOCAL_ZONE = LocalDateTimeFactory.nowInLocalZone();
    public static final LocalDate ISSUE_DATE = NOW_IN_LOCAL_ZONE.toLocalDate().plusDays(1);
    public static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
    public static final LocalDate INTEREST_DATE = NOW_IN_LOCAL_ZONE.toLocalDate().minusDays(101);
}
