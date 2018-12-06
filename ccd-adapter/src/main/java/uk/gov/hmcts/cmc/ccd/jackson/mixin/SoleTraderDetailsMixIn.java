package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1610")
public abstract class SoleTraderDetailsMixIn extends TheirDetailsMixIn {

    @JsonProperty("claimantProvidedTitle")
    abstract String getTitle();

    @JsonProperty("claimantProvidedBusinessName")
    abstract String getBusinessName();
}
