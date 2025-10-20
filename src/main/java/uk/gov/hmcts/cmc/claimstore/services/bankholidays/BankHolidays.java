package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidays {

    @JsonProperty(Countries.ENGLAND_AND_WALES)
    Division englandAndWales;

    static final class Countries {

        static final String ENGLAND_AND_WALES = "england-and-wales";

        private Countries() {
            // NO-OP
        }

    }

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
