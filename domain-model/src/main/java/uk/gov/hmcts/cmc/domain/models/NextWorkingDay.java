package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode
public class NextWorkingDay {

    private final LocalDate date;

    public NextWorkingDay(@JsonProperty("date") LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}
