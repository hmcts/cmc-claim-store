package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1610")
public abstract class StatementOfTruthMixIn {

    @JsonProperty("sotSignerName")
    abstract String getSignerName();

    @JsonProperty("sotSignerRole")
    abstract String getSignerRole();
}
