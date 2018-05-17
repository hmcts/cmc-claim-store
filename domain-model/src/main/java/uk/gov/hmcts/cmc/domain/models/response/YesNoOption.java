package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YesNoOption {
    @JsonProperty("yes")
    YES,
    @JsonProperty("no")
    NO
}
