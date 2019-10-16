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
public class IntentionToProceedServiceTest {

    private IntentionToProceedService intentionToProceedService;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private final int intentionToProceedDeadline = 33;

    @Before
    public void setUp() {
        intentionToProceedService = new IntentionToProceedService(
            workingDayIndicator,
            intentionToProceedDeadline
        );
    }

    @Test
    public void calculateIntentionToProceedDeadlineForWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        when(workingDayIndicator.getNextWorkingDay(any())).then(returnsFirstArg());

        LocalDate deadline = intentionToProceedService.calculateIntentionToProceedDeadline(responseDate);
        assertEquals(responseDate.plusDays(intentionToProceedDeadline), deadline);
    }

    @Test
    public void calculateIntentionToProceedDeadlineForNonWorkday() {
        LocalDate responseDate = LocalDate.of(2019, Month.OCTOBER, 15);
        int nonWorkdayAdjustment = 1;
        when(workingDayIndicator.getNextWorkingDay(any())).then(i -> {
            LocalDate d = i.getArgument(0, LocalDate.class);
            return d.plusDays(nonWorkdayAdjustment);
        });

        LocalDate deadline = intentionToProceedService.calculateIntentionToProceedDeadline(responseDate);
        assertEquals(responseDate.plusDays(intentionToProceedDeadline + nonWorkdayAdjustment), deadline);
    }
}
