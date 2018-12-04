package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SoleTraderMixIn extends PartyMixIn {

    @JsonProperty("partyTitle")
    abstract String getTitle();

    @JsonProperty("partyBusinessName")
    abstract String getBusinessName();
}
