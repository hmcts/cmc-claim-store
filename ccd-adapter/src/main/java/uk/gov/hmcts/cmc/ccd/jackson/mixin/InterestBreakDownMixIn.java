package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public abstract class InterestBreakDownMixIn {

    @JsonProperty("interestBreakDownAmount")
    abstract BigDecimal getTotalAmount();

    @JsonProperty("interestBreakDownExplanation")
    abstract String getExplanation();
}
