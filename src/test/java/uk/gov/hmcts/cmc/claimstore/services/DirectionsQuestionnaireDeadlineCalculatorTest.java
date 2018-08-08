package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDateTime;

@RunWith(MockitoJUnitRunner.class)
public class DirectionsQuestionnaireDeadlineCalculatorTest {

    private static final int SERVICE_DAYS = 5;
    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int END_OF_BUSINESS_DAY = 16;

    private DirectionsQuestionnaireDeadlineCalculator calculator;

    @Mock
    private PublicHolidaysCollection publicHolidaysCollection;

    @Before
    public void setUp() {
        when(publicHolidaysCollection.getPublicHolidays()).thenReturn(new TreeSet<>());

        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(publicHolidaysCollection);

        calculator = new DirectionsQuestionnaireDeadlineCalculator(
            workingDayIndicator, SERVICE_DAYS, DAYS_FOR_RESPONSE, END_OF_BUSINESS_DAY
        );
    }

    @Test
    public void shouldOnlyAdd19DaysWhenWorkingDayAndBefore4pm() {
        LocalDateTime weekdayBefore16 = toDateTime("2018-08-08 15:10");
        LocalDate expected = weekdayBefore16.toLocalDate().plusDays(DAYS_FOR_RESPONSE + SERVICE_DAYS);

        LocalDate dqDeadline = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(weekdayBefore16);

        assertThat(dqDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldAdd19DaysAndOneMoreDayWhenWorkingDayAndAfter4pm() {
        LocalDateTime weekdayAfter16 = toDateTime("2018-08-08 18:10");
        LocalDate expected = weekdayAfter16.toLocalDate().plusDays(DAYS_FOR_RESPONSE + SERVICE_DAYS + 1);

        LocalDate dqDeadline = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(weekdayAfter16);

        assertThat(dqDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldAdd19DaysAnd2MoreDaysWhenSubmittedOnSaturday() {
        LocalDateTime saturday = toDateTime("2018-08-11 10:10");
        LocalDate expected = saturday.toLocalDate().plusDays(DAYS_FOR_RESPONSE + SERVICE_DAYS + 2);

        LocalDate dqDeadline = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(saturday);

        assertThat(dqDeadline).isTheSame(expected);
    }

    @Test
    public void shouldAdd19DaysAndOne3MoreDaysWhenSubmittedOnFridayAfter4pmAndBankHolidayOnMonday() {
        LocalDateTime fridayAfter4pm = toDateTime("2018-08-10 18:10");
        LocalDate bankHolidayOnMonday = fridayAfter4pm.plusDays(3).toLocalDate();
        LocalDate expected = fridayAfter4pm.toLocalDate().plusDays(DAYS_FOR_RESPONSE + SERVICE_DAYS + 4);

        Set<LocalDate> bankHolidays = new TreeSet<>();
        bankHolidays.add(bankHolidayOnMonday);
        when(publicHolidaysCollection.getPublicHolidays()).thenReturn(bankHolidays);

        LocalDate dqDeadline = calculator.calculateDirectionsQuestionnaireDeadlineCalculator(fridayAfter4pm);

        assertThat(dqDeadline).isTheSame(expected);
    }
}
