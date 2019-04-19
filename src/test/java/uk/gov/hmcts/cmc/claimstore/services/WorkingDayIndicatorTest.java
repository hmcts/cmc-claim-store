package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDate;

@RunWith(MockitoJUnitRunner.class)
public class WorkingDayIndicatorTest {

    private static final LocalDate BANK_HOLIDAY = toDate("2017-05-29");
    private static final LocalDate MONDAY = toDate("2017-06-05");
    private static final LocalDate TUESDAY = toDate("2017-06-06");
    private static final LocalDate WEDNESDAY = toDate("2017-06-07");
    private static final LocalDate THURSDAY = toDate("2017-06-08");
    private static final LocalDate FRIDAY = toDate("2017-06-09");
    private static final LocalDate SATURDAY = toDate("2017-06-10");
    private static final LocalDate SUNDAY = toDate("2017-06-11");

    private WorkingDayIndicator service;

    @Mock
    private PublicHolidaysCollection publicHolidaysApiClient;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @Before
    public void setup() {
        service = new WorkingDayIndicator(publicHolidaysApiClient, nonWorkingDaysCollection);
    }

    @Test
    public void shouldReturnTrueForWeekdays() {
        when(publicHolidaysApiClient.getPublicHolidays()).thenReturn(Collections.emptySet());

        assertTrue(service.isWorkingDay(MONDAY));
        assertTrue(service.isWorkingDay(TUESDAY));
        assertTrue(service.isWorkingDay(WEDNESDAY));
        assertTrue(service.isWorkingDay(THURSDAY));
        assertTrue(service.isWorkingDay(FRIDAY));
    }

    @Test
    public void shouldReturnFalseForWeekend() {
        assertFalse(service.isWorkingDay(SATURDAY));
        assertFalse(service.isWorkingDay(SUNDAY));
    }

    @Test
    public void shouldReturnFalseForOneBankHolidayWhenThereIsOneBankHolidayInCollection() {
        LocalDate bankHoliday = BANK_HOLIDAY;
        when(publicHolidaysApiClient.getPublicHolidays())
            .thenReturn(new HashSet<>(Collections.singletonList(bankHoliday)));

        assertFalse(service.isWorkingDay(bankHoliday));
        assertTrue(service.isWorkingDay(MONDAY));
    }

    @Test
    public void shouldReturnFalseForPublicHolidayWhenThereIsMoreDatesInPublicHolidaysCollection() {
        Set<LocalDate> publicHolidays = new HashSet<>(Arrays.asList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY));
        when(publicHolidaysApiClient.getPublicHolidays()).thenReturn(publicHolidays);

        assertTrue(service.isWorkingDay(FRIDAY));

        assertFalse(service.isWorkingDay(MONDAY));
        assertFalse(service.isWorkingDay(TUESDAY));
        assertFalse(service.isWorkingDay(WEDNESDAY));
        assertFalse(service.isWorkingDay(THURSDAY));
    }

    @Test
    public void shouldReturnFalseForWorkingDayExcludedByNonWorkingDaysCollection() {
        assertTrue(service.isWorkingDay(MONDAY));

        when(nonWorkingDaysCollection.contains(MONDAY)).thenReturn(true);

        assertFalse(service.isWorkingDay(MONDAY));
    }
}
