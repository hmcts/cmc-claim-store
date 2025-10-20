package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores all public holidays retrieved from Gov uk API: https://www.gov.uk/bank-holidays.json .
 */
@Component
public class PublicHolidaysCollection {

    private final BankHolidaysApi bankHolidaysApi;

    private Set<LocalDate> cachedPublicHolidays;

    public PublicHolidaysCollection(BankHolidaysApi bankHolidaysApi) {
        this.bankHolidaysApi = bankHolidaysApi;
    }

    private Set<LocalDate> retrieveAllPublicHolidays() {
        BankHolidays value = bankHolidaysApi.retrieveAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BankHolidays.Division.EventDate.FORMAT);

        return value.englandAndWales.events.stream()
            .map(item -> LocalDate.parse(item.date, formatter))
            .collect(Collectors.toSet());
    }

    public Set<LocalDate> getPublicHolidays() {
        if (cachedPublicHolidays == null) {
            cachedPublicHolidays = retrieveAllPublicHolidays();
        }
        return cachedPublicHolidays;
    }
}
