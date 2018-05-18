package uk.gov.hmcts.cmc.rpa;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private DateFormatter() {
        // NO-OP
    }

    public static String format(TemporalAccessor date) {
        return FORMATTER.format(date);
    }
}
