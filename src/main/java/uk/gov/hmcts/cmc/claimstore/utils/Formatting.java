package uk.gov.hmcts.cmc.claimstore.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Formatting {

    private static final String DATE_PATTERN = "d MMMM uuuu";
    private static final String DATE_TIME_PATTERN = "d MMMM uuuu 'at' h:mma";

    private Formatting() {
        // Statics utility class, no instances
    }

    private static String formatTemporalWithPattern(TemporalAccessor temporal, String pattern) {
        requireNonNull(temporal);
        return DateTimeFormatter.ofPattern(pattern).format(temporal);
    }

    public static String formatDate(LocalDate date) {
        return formatTemporalWithPattern(date, DATE_PATTERN);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return formatTemporalWithPattern(dateTime, DATE_PATTERN);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return formatTemporalWithPattern(dateTime, DATE_TIME_PATTERN);
    }

    public static String formatMoney(BigDecimal amount) {
        requireNonNull(amount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB")).format(amount);
    }

    public static String formatMoney(BigInteger amount) {
        requireNonNull(amount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB")).format(amount);
    }

    public static String formatPercent(BigDecimal percent) {
        requireNonNull(percent);
        return format("%s%%", percent.toString());
    }

}
