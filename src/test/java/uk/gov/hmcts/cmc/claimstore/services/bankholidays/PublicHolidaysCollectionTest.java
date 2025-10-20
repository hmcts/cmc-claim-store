package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDate;

@RunWith(MockitoJUnitRunner.class)
public class PublicHolidaysCollectionTest {

    private static final String BANK_HOLIDAY_1 = "2010-10-10";
    private static final String BANK_HOLIDAY_2 = "2011-11-11";

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    @Test
    public void getAllBankHolidaysShouldBeOk() {
        //given
        when(bankHolidaysApi.retrieveAll()).thenReturn(createExpectedResponse());
        PublicHolidaysCollection publicHolidaysCollection = new PublicHolidaysCollection(bankHolidaysApi);

        //when
        Set<LocalDate> response = publicHolidaysCollection.getPublicHolidays();

        //then
        assertThat(response).isNotEmpty();
        assertThat(response.contains(toDate(BANK_HOLIDAY_1))).isTrue();
        assertThat(response.contains(toDate(BANK_HOLIDAY_2))).isTrue();
    }

    @Test
    public void externalApiShouldBeCalledOnlyOnce() {
        // given
        when(bankHolidaysApi.retrieveAll()).thenReturn(createExpectedResponse());
        PublicHolidaysCollection publicHolidaysCollection = new PublicHolidaysCollection(bankHolidaysApi);

        // when
        Set<LocalDate> resultFromApi = publicHolidaysCollection.getPublicHolidays();
        Set<LocalDate> resultFromCache = publicHolidaysCollection.getPublicHolidays();
        Set<LocalDate> resultFromCacheAgain = publicHolidaysCollection.getPublicHolidays();

        // then
        verify(bankHolidaysApi, times(1)).retrieveAll();
        assertThat(resultFromApi).isSameAs(resultFromCache).isSameAs(resultFromCacheAgain);
    }

    private static BankHolidays createExpectedResponse() {
        BankHolidays expResponse = new BankHolidays();
        expResponse.englandAndWales = new BankHolidays.Division();
        BankHolidays.Division.EventDate item1 = createItem(BANK_HOLIDAY_1);
        BankHolidays.Division.EventDate item2 = createItem(BANK_HOLIDAY_2);
        expResponse.englandAndWales.events = new ArrayList<>(Arrays.asList(item1, item2));

        return expResponse;
    }

    private static BankHolidays.Division.EventDate createItem(String date) {
        BankHolidays.Division.EventDate item = new BankHolidays.Division.EventDate();
        item.date = date;

        return item;
    }
}
