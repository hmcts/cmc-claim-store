package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public abstract class IndividualMixIn extends PartyMixIn {

    @JsonProperty("partyDateOfBirth")
    abstract LocalDate getDateOfBirth();
}
