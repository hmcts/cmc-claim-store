package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MappingUtils {

    private MappingUtils() {
        // utils class. No instances
    }

    public static LocalDateTime toLocalDateTimeFromUTC(Timestamp input) {
        return LocalDateTimeFactory.fromUTC(input.toLocalDateTime());
    }

    public static Long toNullableLong(Integer input) {
        return input != null ? input.longValue() : null;
    }

    public static LocalDateTime toNullableLocalDateTimeFromUTC(Timestamp input) {
        return input != null ? toLocalDateTimeFromUTC(input) : null;
    }

    public static LocalDate toNullableLocalDateFromUTC(Timestamp input) {
        return input != null ? toLocalDateTimeFromUTC(input).toLocalDate() : null;
    }
}
