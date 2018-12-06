package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1610")
public interface OrganisationMixIn extends PartyMixIn {

    @JsonProperty("partyContactPerson")
    abstract String getContactPerson();

    @JsonProperty("partyCompaniesHouseNumber")
    abstract String getCompaniesHouseNumber();
}
