package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderGenerationDeadlinesCalculatorTest {
    private static final int DAYS_FOR_RESPONSE = 28;
    private static final int DAYS_FOR_SERVICE = 5;
    private static final LocalDate TODAY = LocalDate.of(2019, 1, 2);

    private LegalOrderGenerationDeadlinesCalculator calculator;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private Clock clock;

    @Before
    public void setUp() {
        calculator = new LegalOrderGenerationDeadlinesCalculator(
            clock,
            workingDayIndicator);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    public void shouldCalculateDeadline() {
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

        LocalDate responseDeadline = calculator.calculateOrderGenerationDeadlines();
        LocalDate expected = TODAY.plusDays(DAYS_FOR_RESPONSE + DAYS_FOR_SERVICE);

        assertThat(responseDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldCalculateDeadlineAsTheNextWorkingDayIfDeadlineIsOnAHoliday() {
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false).thenReturn(true);
        LocalDate expected = TODAY.plusDays(DAYS_FOR_RESPONSE + DAYS_FOR_SERVICE + 1);
        LocalDate responseDeadline = calculator.calculateOrderGenerationDeadlines();

        assertThat(responseDeadline).isTheSame(expected);
    }
}
