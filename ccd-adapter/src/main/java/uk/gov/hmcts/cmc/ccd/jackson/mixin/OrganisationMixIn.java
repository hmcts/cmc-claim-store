package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class OrganisationMixIn extends PartyMixIn {

    @JsonProperty("partyContactPerson")
    abstract String getContactPerson();

    @JsonProperty("partyCompaniesHouseNumber")
    abstract String getCompaniesHouseNumber();
}
