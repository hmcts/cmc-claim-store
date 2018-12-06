package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public interface InterestBreakDownMixIn {

    @JsonProperty("interestBreakDownAmount")
     BigDecimal getTotalAmount();

    @JsonProperty("interestBreakDownExplanation")
     String getExplanation();
}
