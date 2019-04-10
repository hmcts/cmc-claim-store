package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DirectionsQuestionnaireDeadlineCalculatorTest {

    private static final int SERVICE_DAYS = 5;
    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int END_OF_BUSINESS_DAY = 16;

    private static final LocalDate SAMPLE_WEDNESDAY = LocalDate.of(2010, 1, 1);
    private static final LocalTime BEFORE_EOB = LocalTime.of(END_OF_BUSINESS_DAY - 1, 0);
    private static final LocalTime AFTER_EOB = LocalTime.of(END_OF_BUSINESS_DAY + 1, 0);

    private DirectionsQuestionnaireDeadlineCalculator calculator;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @Before
    public void setUp() {
        when(publicHolidaysCollection.getPublicHolidays()).thenReturn(new TreeSet<>());

        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            publicHolidaysCollection, nonWorkingDaysCollection
        );

        calculator = new DirectionsQuestionnaireDeadlineCalculator(
            workingDayIndicator, SERVICE_DAYS, DAYS_FOR_RESPONSE, END_OF_BUSINESS_DAY
        );
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
    }

    @Test
    public void shouldAddUsualDaysWhenWorkingDayAndBefore4pm() {
        LocalDateTime input = SAMPLE_WEDNESDAY.atTime(BEFORE_EOB);
        LocalDate expected = input.plusDays(SERVICE_DAYS + DAYS_FOR_RESPONSE).toLocalDate();

        LocalDate actual = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(input);
        assertThat(actual).isTheSame(expected);
    }

    @Test
    public void shouldAddUsualDaysPlusOneWhenWorkingDayAndAfter4pm() {
        LocalDateTime input = SAMPLE_WEDNESDAY.atTime(AFTER_EOB);
        LocalDate expected = input.plusDays(SERVICE_DAYS + DAYS_FOR_RESPONSE + 1).toLocalDate();

        LocalDate actual = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(input);
        assertThat(actual).isTheSame(expected);
    }

    @Test
    public void shouldAddDaysUntilWorkingDay() {
        LocalDateTime input = SAMPLE_WEDNESDAY.atTime(BEFORE_EOB);

        LocalDate usualResult = input.plusDays(SERVICE_DAYS + DAYS_FOR_RESPONSE).toLocalDate();
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, false, false, true);
        LocalDate expected = usualResult.plusDays(3);

        LocalDate actual = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(input);
        assertThat(actual).isTheSame(expected);
    }

    @Test
    public void shouldAddDaysToAvoidNonWorkingDays() {
        LocalDateTime fridayBefore4pm = toDateTime("2018-08-10 12:10");
        // +3 non-working days, +2 weekend days
        LocalDate expected = fridayBefore4pm.toLocalDate().plusDays(DAYS_FOR_RESPONSE + SERVICE_DAYS + 3 + 2);

        when(nonWorkingDaysCollection.contains(any(LocalDate.class)))
            .thenReturn(true, true, true, false);

        LocalDate dqDeadline = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(fridayBefore4pm);

        assertThat(dqDeadline).isTheSame(expected);
    }
}
