package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@SuppressWarnings("squid:S1610")
public abstract class IndividualDetailsMixIn extends TheirDetailsMixIn {
    @JsonProperty("claimantProvidedDateOfBirth")
    abstract LocalDate getDateOfBirth();
}
