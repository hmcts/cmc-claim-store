package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S1610")
public abstract class StatementOfTruthMixIn {

    @JsonProperty("statementOfTruthSignerName")
    abstract String getSignerName();

    @JsonProperty("statementOfTruthSignerRole")
    abstract String getSignerRole();
}
