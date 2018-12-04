package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;

public abstract class InterestBreakDownMixIn {

    @JsonProperty("interestBreakDownAmount")
    abstract BigDecimal getTotalAmount();

    @JsonProperty("interestBreakDownCalc")
    abstract String getExplanation();
}
