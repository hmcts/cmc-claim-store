package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface IndividualDetailsMixIn extends TheirDetailsMixIn {
    @JsonProperty("claimantProvidedDateOfBirth")
    LocalDate getDateOfBirth();
}
