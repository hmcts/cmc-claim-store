package uk.gov.hmcts.cmc.claimstore.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.clients.RestClient;
import uk.gov.hmcts.cmc.claimstore.processors.RestClientFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores all public holidays retrieved from Gov uk API: https://www.gov.uk/bank-holidays.json .
 */
@Component
public class PublicHolidaysCollection {

    static class Endpoints {
        static final String BANK_HOLIDAYS = "/bank-holidays.json";

        private Endpoints() {
            // NO-OP
        }

    }

    private final RestClient client;

    private Set<LocalDate> cachedPublicHolidays;

    public PublicHolidaysCollection(
        final RestClientFactory clientFactory,
        @Value("${bankHolidays.api.url}") final String url) {
        this.client = clientFactory.create(url);
    }

    private Set<LocalDate> retrieveAllPublicHolidays() {
        BankHolidays value = client.get(Endpoints.BANK_HOLIDAYS, BankHolidays.class);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BankHolidays.Division.EventDate.FORMAT);

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class BankHolidays {
        static final class Countries {

            static final String ENGLAND_AND_WALES = "england-and-wales";

            private Countries() {
                // NO-OP
            }

        }

        @JsonProperty(Countries.ENGLAND_AND_WALES)
        Division englandAndWales;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Division {
            @JsonProperty("events")
            List<EventDate> events;

            @JsonIgnoreProperties(ignoreUnknown = true)
            static class EventDate {
                static final String FORMAT = "yyyy-MM-dd";

                @JsonProperty("date")
                String date;
            }
        }
    }
}
