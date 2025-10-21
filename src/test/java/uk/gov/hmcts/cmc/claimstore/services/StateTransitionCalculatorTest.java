package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StateTransitionCalculatorTest {

    private StateTransitionCalculator stateTransitionCalculator;

    private final int numberOfDays = 33;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Before
    public void setUp() {
        stateTransitionCalculator = new StateTransitionCalculator(workingDayIndicator, numberOfDays);
    }

    @Test
    public void calculateDeadlineFromDateForWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        when(workingDayIndicator.getNextWorkingDay(any())).then(returnsFirstArg());

        LocalDate deadline = stateTransitionCalculator.calculateDeadlineFromDate(responseDate);
        assertEquals(responseDate.plusDays(numberOfDays), deadline);
    }

    @Test
    public void calculateDeadlineFromDateForNonWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        int nonWorkdayAdjustment = 1;
        when(workingDayIndicator.getNextWorkingDay(any())).then(i -> {
            LocalDate d = i.getArgument(0, LocalDate.class);
            return d.plusDays(nonWorkdayAdjustment);
        });

        LocalDate deadline = stateTransitionCalculator.calculateDeadlineFromDate(responseDate);
        assertEquals(responseDate.plusDays(numberOfDays + nonWorkdayAdjustment), deadline);
    }

    @Test
    public void calculateDateFromDeadlineOnAWorkdayAfter4pm() {
        LocalDateTime tuesdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 16, 0, 0);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        LocalDate actual = stateTransitionCalculator.calculateDateFromDeadline(tuesdayAfter4pm);

        LocalDate expected = tuesdayAfter4pm.toLocalDate().minusDays(numberOfDays);
        assertEquals(expected, actual);

    }

    @Test
    public void calculateDateFromDeadlineOnAWorkdayBefore4pm() {
        LocalDateTime tuesdayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 15, 59, 59);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        LocalDate actual = stateTransitionCalculator.calculateDateFromDeadline(tuesdayBefore4pm);

        LocalDate expected = tuesdayBefore4pm.toLocalDate().minusDays(numberOfDays + 1);
        assertEquals(expected, actual);
    }

    @Test
    public void calculateDateFromDeadlineONonWorkdayAfter4pm() {
        LocalDateTime saturday = LocalDateTime.of(2019, Month.OCTOBER, 12, 16, 0, 0);

        int workdayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(saturday.minusDays(workdayAdjustment).toLocalDate());

        LocalDate actual = stateTransitionCalculator.calculateDateFromDeadline(saturday);

        LocalDate expected = saturday.toLocalDate().minusDays(numberOfDays + workdayAdjustment);
        assertEquals(expected, actual);
    }

    @Test
    public void calculateDateFromDeadlineOnDayAfterNonWorkdayBefore4pm() {
        LocalDateTime mondayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 14, 15, 59, 59);
        int workdayAdjustment = 2;
        int timeOfDayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(mondayBefore4pm.minusDays(workdayAdjustment + timeOfDayAdjustment).toLocalDate());

        LocalDate actual = stateTransitionCalculator.calculateDateFromDeadline(mondayBefore4pm);

        LocalDate expected = mondayBefore4pm.toLocalDate()
            .minusDays(numberOfDays + timeOfDayAdjustment + workdayAdjustment);
        assertEquals(expected, actual);
    }

}
