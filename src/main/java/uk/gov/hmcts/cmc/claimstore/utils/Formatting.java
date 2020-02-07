package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Formatting {

    private static final String DATE_PATTERN = "d MMMM uuuu";
    private static final String DATE_TIME_PATTERN = "d MMMM uuuu 'at' h:mma";
    private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-GB"));
    private static final DecimalFormat decimalFormat = new DecimalFormat("£###,###.##");

    private Formatting() {
        // Statics utility class, no instances
    }

    private static String formatTemporalWithPattern(TemporalAccessor temporal, String pattern) {
        requireNonNull(temporal);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
            .withZone(LocalDateTimeFactory.LOCAL_ZONE);

        if (temporal instanceof ChronoLocalDateTime) {
            ChronoZonedDateTime<?> chronoZonedDateTime =
                ((ChronoLocalDateTime<?>) temporal).atZone(LocalDateTimeFactory.UTC_ZONE);
            return dateTimeFormatter.format(chronoZonedDateTime);

        } else {
            return dateTimeFormatter.format(temporal);

        }
    }

    public static String formatDate(LocalDate date) {
        return formatTemporalWithPattern(date, DATE_PATTERN);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return formatTemporalWithPattern(dateTime, DATE_PATTERN);
    }

    private static boolean isWholeNumber(BigDecimal amount) {
        return amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return formatTemporalWithPattern(dateTime, DATE_TIME_PATTERN);
    }

    public static String formatMoney(BigDecimal amount) {
        requireNonNull(amount);
        if (isWholeNumber(amount)) {
            return decimalFormat.format(amount);
        } else {
            return numberFormat.format(amount);
        }
    }

    public static String formatMoney(BigInteger amount) {
        requireNonNull(amount);
        return numberFormat.format(amount);
    }

    public static String formatPercent(BigDecimal percent) {
        requireNonNull(percent);
        return format("%s%%", percent.toString());
    }

}
