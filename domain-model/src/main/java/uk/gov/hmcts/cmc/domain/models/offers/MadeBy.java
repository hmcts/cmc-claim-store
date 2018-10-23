package uk.gov.hmcts.cmc.domain.models.offers;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MadeBy {
    @JsonProperty("CLAIMANT")
    CLAIMANT,
    @JsonProperty("DEFENDANT")
    DEFENDANT,
    COURT
}
