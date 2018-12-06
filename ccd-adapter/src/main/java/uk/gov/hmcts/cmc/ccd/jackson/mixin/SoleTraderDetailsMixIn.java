package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SoleTraderDetailsMixIn extends TheirDetailsMixIn {

    @JsonProperty("claimantProvidedTitle")
    String getTitle();

    @JsonProperty("claimantProvidedBusinessName")
    String getBusinessName();
}
