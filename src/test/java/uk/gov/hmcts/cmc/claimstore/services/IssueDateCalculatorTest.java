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

public class IssueDateCalculatorTest {

    private static final boolean YES = true;
    private static final boolean NO = false;

    private static final LocalDateTime WEEKDAY_MORNING = toDateTime("2017-05-08 09:30");
    private static final LocalDateTime WEEKDAY_EVENING = toDateTime("2017-05-08 16:01");
    private static final LocalDateTime FRIDAY_EVENING = toDateTime("2017-05-12 16:01");
    private static final LocalDateTime GOOD_FRIDAY_MORNING = toDateTime("2017-04-14 09:01");
    private static final LocalDateTime GOOD_THURSDAY_EVENING = toDateTime("2017-04-13 16:01");
    private static final LocalDateTime SAT_MORNING = toDateTime("2017-05-06 09:30");
    private static final LocalDateTime SAT_EVENING = toDateTime("2017-05-06 18:30");
    private static final LocalDateTime SUN_MORNING = toDateTime("2017-05-07 00:01");
    private static final LocalDateTime SUN_EVENING = toDateTime("2017-05-07 19:30");

    private static final int CLOSE_OFFICE_HOUR = 16;

    private IssueDateCalculator calculator;
    private WorkingDayIndicator workingDayIndicator;

    @Before
    public void setUp() {
        workingDayIndicator = mock(WorkingDayIndicator.class);
        calculator = new IssueDateCalculator(workingDayIndicator, CLOSE_OFFICE_HOUR);
    }

    @Test
    public void submittedOnWeekdayMorningNoPublicHolidaysShouldBeIssuedTheSameDay() {

        noHolidays();

        LocalDate issueDate = calculator.calculateIssueDay(WEEKDAY_MORNING);

        assertThat(issueDate).isTheSame(WEEKDAY_MORNING).isWeekday();
    }

    @Test
    public void submittedOnMondayEveningNoPublicHolidaysShouldBeIssuedNextDay() {

        noHolidays();

        LocalDate issueDate = calculator.calculateIssueDay(WEEKDAY_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(1, WEEKDAY_MORNING);
    }

    @Test
    public void submittedOnFridayEveningNoPublicHolidaysShouldBeIssuedAfterWeekend() {

        // Sat, Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(FRIDAY_EVENING);

        assertThat(issueDate).isMonday().isNumberOfDaysSince(3, FRIDAY_EVENING);
    }

    @Test
    public void submittedOnGoodFridayMorningShouldBeIssuedBeIssuedOnTuesdayInFourDays() {

        // Fri, Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(GOOD_FRIDAY_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(4, GOOD_FRIDAY_MORNING);
    }

    @Test
    public void submittedOnGoodThursdayEveningShouldBeIssuedBeIssuedOnTuesdayInFiveDays() {

        // Fri, Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(GOOD_THURSDAY_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(5, GOOD_THURSDAY_EVENING);
    }

    @Test
    public void submittedOnSaturdayMorningShouldBeIssuedAfterWeekend() {

        // Sat, Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SAT_MORNING);

        assertThat(issueDate).isMonday().isNumberOfDaysSince(2, SAT_MORNING);
    }

    @Test
    public void submittedOnSaturdayMorningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sat, Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SAT_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(3, SAT_MORNING);
    }

    @Test
    public void submittedOnSaturdayEveningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SAT_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(3, SAT_EVENING);
    }

    @Test
    public void submittedOnSundayMorningShouldBeIssuedNextDay() {

        // Sun, Mon
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SUN_MORNING);

        assertThat(issueDate).isMonday().isNumberOfDaysSince(1, SUN_MORNING);
    }

    @Test
    public void submittedOnSundayMorningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Sun, Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SUN_MORNING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(2, SUN_MORNING);
    }

    @Test
    public void submittedOnSundayEveningBankHolidayOnMondayShouldBeIssuedOnTuesday() {

        // Mon, Tue
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

        LocalDate issueDate = calculator.calculateIssueDay(SUN_EVENING);

        assertThat(issueDate).isTuesday().isNumberOfDaysSince(2, SUN_EVENING);
    }

    private void noHolidays() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
    }
}
