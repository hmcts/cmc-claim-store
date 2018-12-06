package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface IndividualMixIn extends PartyMixIn {

    @JsonProperty("partyDateOfBirth")
    LocalDate getDateOfBirth();
}
