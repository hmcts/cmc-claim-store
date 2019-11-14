package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntentionToProceedDeadlineCalculatorTest {

    private IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator;

    private final int intentionToProceedAdjustment = 33;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Before
    public void setUp() {
        intentionToProceedDeadlineCalculator = new IntentionToProceedDeadlineCalculator(
            workingDayIndicator,
            intentionToProceedAdjustment
        );
    }

    @Test
    public void calculateIntentionToProceedDeadlineForWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        when(workingDayIndicator.getNextWorkingDay(any())).then(returnsFirstArg());

        LocalDate deadline = intentionToProceedDeadlineCalculator.calculateIntentionToProceedDeadline(responseDate);
        assertEquals(responseDate.plusDays(intentionToProceedAdjustment), deadline);
    }

    @Test
    public void calculateIntentionToProceedDeadlineForNonWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        int nonWorkdayAdjustment = 1;
        when(workingDayIndicator.getNextWorkingDay(any())).then(i -> {
            LocalDate d = i.getArgument(0, LocalDate.class);
            return d.plusDays(nonWorkdayAdjustment);
        });

        LocalDate deadline = intentionToProceedDeadlineCalculator.calculateIntentionToProceedDeadline(responseDate);
        assertEquals(responseDate.plusDays(intentionToProceedAdjustment + nonWorkdayAdjustment), deadline);
    }

}
