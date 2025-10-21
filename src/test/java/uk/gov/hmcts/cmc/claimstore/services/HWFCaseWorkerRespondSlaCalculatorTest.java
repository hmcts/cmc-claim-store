package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDateTime;

public class HWFCaseWorkerRespondSlaCalculatorTest {

    private static final boolean YES = true;
    private static final boolean NO = false;

    private static final LocalDateTime WEEKDAY_MORNING = toDateTime("2017-05-08 09:30");
    private static final LocalDateTime WEEKDAY_MORNING_NEW = toDateTime("2017-05-15 09:30");
    private static final LocalDateTime WEEKDAY_EVENING = toDateTime("2017-05-08 16:01");
    private static final LocalDateTime FRIDAY_EVENING = toDateTime("2017-05-12 16:01");
    //private static final LocalDateTime FRIDAY_EVENING = toDateTime("2021-01-08 16:01");
    private static final LocalDateTime GOOD_FRIDAY_MORNING = toDateTime("2017-04-14 09:01");
    private static final LocalDateTime GOOD_THURSDAY_EVENING = toDateTime("2017-04-13 16:01");
    private static final LocalDateTime SAT_MORNING = toDateTime("2017-05-06 09:30");
    private static final LocalDateTime SAT_EVENING = toDateTime("2017-05-06 18:30");
    private static final LocalDateTime SUN_MORNING = toDateTime("2017-05-07 00:01");
    private static final LocalDateTime SUN_EVENING = toDateTime("2017-05-07 19:30");

    private static final int CLOSE_OFFICE_HOUR = 16;
    private static final int TIME_RESPONSE_DAYS = 5;

    private HWFCaseWorkerRespondSlaCalculator calculator;
    private WorkingDayIndicator workingDayIndicator;

    @Before
    public void setUp() {
        workingDayIndicator = mock(WorkingDayIndicator.class);
        calculator = new HWFCaseWorkerRespondSlaCalculator(workingDayIndicator, TIME_RESPONSE_DAYS, CLOSE_OFFICE_HOUR);
    }

    @Test
    public void submittedOnWeekdayMorningNoPublicHolidaysShouldBeIssuedTheSameDay() {

        noHolidays();

        LocalDate issueDate = calculator.calculate(WEEKDAY_MORNING);

        assertThat(issueDate).isTheSame(WEEKDAY_MORNING_NEW).isWeekday();
    }

    @Test
    public void submittedOnMondayEveningNoPublicHolidaysShouldBeIssuedNextDay() {

        noHolidays();

        LocalDate issueDate = calculator.calculate(WEEKDAY_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(8, WEEKDAY_MORNING);
    }

    @Test
    public void submittedOnFridayEveningNoPublicHolidaysShouldBeIssuedAfterWeekend() {

        // Sat, Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculate(FRIDAY_EVENING);
        assertThat(issueDate).isMonday().isNumberOfDaysSince(10, FRIDAY_EVENING);
    }

    @Test
    public void submittedOnGoodFridayMorningShouldBeIssuedBeIssuedOnTuesdayInFourDays() {

        // Fri, Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculate(GOOD_FRIDAY_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(11, GOOD_FRIDAY_MORNING);
    }

    @Test
    public void submittedOnGoodThursdayEveningShouldBeIssuedBeIssuedOnTuesdayInFiveDays() {

        // Fri, Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculate(GOOD_THURSDAY_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(12, GOOD_THURSDAY_EVENING);
    }

    @Test
    public void submittedOnSaturdayMorningShouldBeIssuedAfterWeekend() {

        // Sat, Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculate(SAT_MORNING);

        assertThat(issueDate).isMonday().isNumberOfDaysSince(9, SAT_MORNING);
    }

    @Test
    public void submittedOnSaturdayMorningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculate(SAT_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(10, SAT_MORNING);
    }

    @Test
    public void submittedOnSaturdayEveningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculate(SAT_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(10, SAT_EVENING);
    }

    @Test
    public void submittedOnSundayMorningShouldBeIssuedNextDay() {

        // Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

        LocalDate issueDate = calculator.calculate(SUN_MORNING);

        assertThat(issueDate).isMonday().isNumberOfDaysSince(8, SUN_MORNING);
    }

    @Test
    public void submittedOnSundayMorningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculate(SUN_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(9, SUN_MORNING);
    }

    @Test
    public void submittedOnSundayEveningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

        LocalDate issueDate = calculator.calculate(SUN_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(9, SUN_EVENING);
    }

    private void noHolidays() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
    }
}
