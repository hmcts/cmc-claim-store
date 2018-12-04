package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class StatementOfTruthMixIn {

    @JsonProperty("sotSignerName")
    abstract String getSignerName();

    @JsonProperty("sotSignerRole")
    abstract String getSignerRole();
}
