package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1610")
public abstract class OrganisationDetailsMixIn extends TheirDetailsMixIn {
    @JsonProperty("claimantProvidedContactPerson")
    abstract String getContactPerson();

    @JsonProperty("claimantProvidedCompaniesHouseNumber")
    abstract String getCompaniesHouseNumber();
}
