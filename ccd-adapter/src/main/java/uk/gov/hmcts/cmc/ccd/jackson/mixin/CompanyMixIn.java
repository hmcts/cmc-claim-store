package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CompanyMixIn extends PartyMixIn {

    @JsonProperty("partyContactPerson")
    abstract String getContactPerson();
}
