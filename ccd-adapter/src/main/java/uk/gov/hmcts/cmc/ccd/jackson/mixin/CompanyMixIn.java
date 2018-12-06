package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface CompanyMixIn extends PartyMixIn {

    @JsonProperty("partyContactPerson")
     String getContactPerson();
}
