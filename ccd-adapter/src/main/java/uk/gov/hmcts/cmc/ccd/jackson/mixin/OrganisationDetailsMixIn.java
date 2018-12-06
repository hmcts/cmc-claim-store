package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public interface OrganisationDetailsMixIn extends TheirDetailsMixIn {
    @JsonProperty("claimantProvidedContactPerson")
    String getContactPerson();

    @JsonProperty("claimantProvidedCompaniesHouseNumber")
    Optional<String> getCompaniesHouseNumber();
}
