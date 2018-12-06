package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface StatementOfTruthMixIn {

    @JsonProperty("sotSignerName")
    String getSignerName();

    @JsonProperty("sotSignerRole")
    String getSignerRole();
}
