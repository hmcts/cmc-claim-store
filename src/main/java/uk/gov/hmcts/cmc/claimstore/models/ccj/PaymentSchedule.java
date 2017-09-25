package uk.gov.hmcts.cmc.claimstore.models.ccj;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentSchedule {
    @JsonProperty("EACH_WEEK")
    EACH_WEEK,
    @JsonProperty("EVERY_TWO_WEEKS")
    EVERY_TWO_WEEKS,
    @JsonProperty("EVERY_MONTH")
    EVERY_MONTH
}
