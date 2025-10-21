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
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDate;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDateTime;

@RunWith(MockitoJUnitRunner.class)
public class OfferResponseDeadlineCalculatorTest {

    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int END_OF_BUSINESS_DAY = 16;

    private OfferResponseDeadlineCalculator calculator;

    @Mock
    private PublicHolidaysCollection publicHolidaysCollection;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @Before
    public void setUp() {

        when(publicHolidaysCollection.getPublicHolidays()).thenReturn(new TreeSet<>());

        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            publicHolidaysCollection,
            nonWorkingDaysCollection
        );

        calculator = new OfferResponseDeadlineCalculator(
            workingDayIndicator, DAYS_FOR_RESPONSE, END_OF_BUSINESS_DAY
        );
    }

    @Test
    public void shouldOnlyAddNumberOfGivenDaysWhenSubmittedOnWeekdayBeforeEob() {

        LocalDateTime weekdayBefore16 = toDateTime("2017-10-10 00:10");
        LocalDate expected = toDate("2017-10-24");

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayBefore16);

        assertThat(responseDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldOnlyAddNumberOfGivenDaysWhenSubmittedOnWeekdayAt1559() {

        LocalDateTime weekdayBefore16 = toDateTime("2017-10-10 15:59");
        LocalDate expected = toDate("2017-10-24");

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayBefore16);

        assertThat(responseDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldAddOneMoreDayWhenSubmittedOnWeekdayAt16() {

        LocalDateTime weekdayAt16 = toDateTime("2017-10-10 16:00");
        LocalDate expected = toDate("2017-10-25");

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayAt16);

        assertThat(responseDeadline).isWeekday().isTheSame(expected);
    }

    @Test
    public void shouldAddMoreDaysWhenSubmittedOnFridayAfter16() {

        LocalDateTime weekdayAfter16 = toDateTime("2017-10-20 18:00");
        LocalDate expected = toDate("2017-11-06");

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayAfter16);

        assertThat(responseDeadline).isMonday().isTheSame(expected);
    }

    @Test
    public void shouldAddMoreDaysWhenSubmittedOnFridayAfter16AndMondayIsBankHoliday() {

        Set<LocalDate> bankHolidays = new TreeSet<>();
        bankHolidays.add(toDate("2017-08-28"));
        when(publicHolidaysCollection.getPublicHolidays()).thenReturn(bankHolidays);

        LocalDateTime weekdayAfter16 = toDateTime("2017-08-11 18:00");
        LocalDate expected = toDate("2017-08-29");

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayAfter16);

        assertThat(responseDeadline).isTuesday().isTheSame(expected);
    }

    @Test
    public void shouldAddMoreDaysToSkipNonWorkingDays() {
        LocalDateTime weekdayAfter16 = toDateTime("2017-08-11 18:00");
        LocalDate expected = toDate("2017-08-31");

        when(nonWorkingDaysCollection.contains(any(LocalDate.class)))
            .thenReturn(true, true, true, false);

        LocalDate responseDeadline = calculator.calculateOfferResponseDeadline(weekdayAfter16);

        assertThat(responseDeadline).isThursday().isTheSame(expected);
    }

}
