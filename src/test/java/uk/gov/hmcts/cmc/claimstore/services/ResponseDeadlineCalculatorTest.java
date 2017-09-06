package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

public class ResponseDeadlineCalculatorTest {

    private static final boolean YES = true;
    private static final boolean NO = false;

    private static final LocalDate ISSUED_ON_TUE = toDate("2017-05-09");
    private static final LocalDate ISSUED_ON_MON = toDate("2017-05-08");
    private static final LocalDate ISSUED_ON_MON_BEFORE_GOOD_FRIDAY = toDate("2017-03-27");

    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int DAYS_FOR_SERVICE = 4;
    private static final int POSTPONE_BY = 14;

    private ResponseDeadlineCalculator calculator;
    private WorkingDayIndicator workingDayIndicator;

    @Before
    public void setUp() {
        workingDayIndicator = mock(WorkingDayIndicator.class);
        calculator = new ResponseDeadlineCalculator(
            workingDayIndicator, DAYS_FOR_SERVICE, DAYS_FOR_RESPONSE, POSTPONE_BY
        );
    }

    @Test
    public void calculateResponseDeadlineWhenNoBankHolidaysShouldReturnFridayAfter21Days() {

        noHolidays();

        LocalDate responseDeadline = calculator.calculateResponseDeadline(ISSUED_ON_MON);

        assertThat(responseDeadline).isFriday().isNumberOfDaysSince(18, ISSUED_ON_MON);
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnSaturdayThenReturnFirstWorkingDay() {

        // Sat, Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate responseDeadline = calculator.calculateResponseDeadline(ISSUED_ON_TUE);

        assertThat(responseDeadline).isMonday().isNumberOfDaysSince(20, ISSUED_ON_TUE);
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnSaturdayBeforeBankHolidayThenReturnFirstWorkingDay() {

        // Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, YES);

        LocalDate responseDeadline = calculator.calculateResponseDeadline(ISSUED_ON_TUE);

        assertThat(responseDeadline).isTuesday().isNumberOfDaysSince(21, ISSUED_ON_TUE);
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnGoodFridayThenReturnFirstWorkingDay() {

        // good Fri, Sat, Sun, Easter Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

        LocalDate responseDeadline = calculator.calculateResponseDeadline(ISSUED_ON_MON_BEFORE_GOOD_FRIDAY);

        assertThat(responseDeadline)
            .isTuesday()
            .isNumberOfDaysSince(22, ISSUED_ON_MON_BEFORE_GOOD_FRIDAY);
    }

    @Test
    public void calculatePostponedResponseDeadlineWhenNoBankHolidaysShouldReturnFridayAfter21Days() {

        noHolidays();

        LocalDate responseDeadline = calculator.calculateResponseDeadline(ISSUED_ON_MON);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(responseDeadline);

        assertThat(postponedDeadline).isFriday().isNumberOfDaysSince(14, responseDeadline);
    }

    private void noHolidays() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
    }

    private static LocalDate toDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
