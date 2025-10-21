package uk.gov.hmcts.cmc.claimstore.utils;

import org.assertj.core.api.AbstractAssert;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DayAssert extends AbstractAssert<DayAssert, LocalDate> {

    public DayAssert(LocalDate actual) {
        super(actual, DayAssert.class);
    }

    public static DayAssert assertThat(LocalDate actual) {
        return new DayAssert(actual);
    }

    public static DayAssert assertThat(LocalDateTime actual) {
        return new DayAssert(actual.toLocalDate());
    }

    public DayAssert isWeekday() {
        DayOfWeek day = actual.getDayOfWeek();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            failWithMessage("Expected weekday but was <%s>", actual.getDayOfWeek());
        }

        return this;
    }

    public DayAssert isMonday() {
        return isDay(DayOfWeek.MONDAY);
    }

    public DayAssert isTuesday() {
        return isDay(DayOfWeek.TUESDAY);
    }

    public DayAssert isWednesday() {
        return isDay(DayOfWeek.WEDNESDAY);
    }

    public DayAssert isThursday() {
        return isDay(DayOfWeek.THURSDAY);
    }

    public DayAssert isFriday() {
        return isDay(DayOfWeek.FRIDAY);
    }

    private DayAssert isDay(DayOfWeek expectedDay) {
        isNotNull();

        if (actual.getDayOfWeek() != expectedDay) {
            failWithMessage("Expected <%s> but was <%s>", expectedDay, actual.getDayOfWeek());
        }

        return this;
    }

    public DayAssert isTheSame(LocalDate date) {
        isNotNull();

        if (!actual.isEqual(date)) {
            failWithMessage("Expected <%s> but was <%s>", date.toString(), actual.toString());
        }

        return this;
    }

    public DayAssert isTheSame(LocalDateTime date) {
        return isTheSame(date.toLocalDate());
    }

    public DayAssert isNumberOfDaysSince(int expectedDiff, LocalDate issuedOn) {
        isNotNull();

        long actuallyDaysBetween = ChronoUnit.DAYS.between(issuedOn, actual);

        if (actuallyDaysBetween != expectedDiff) {
            failWithMessage("Expected <%d> but was <%d>", expectedDiff, actuallyDaysBetween);
        }

        return this;
    }

    public DayAssert isNumberOfDaysSince(int expectedDiff, LocalDateTime issuedOn) {
        return isNumberOfDaysSince(expectedDiff, issuedOn.toLocalDate());
    }
}
