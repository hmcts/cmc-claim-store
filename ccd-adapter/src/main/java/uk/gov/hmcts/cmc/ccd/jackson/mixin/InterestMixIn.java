package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest.InterestType;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;

public abstract class InterestMixIn {

    @JsonProperty("interestType")
    abstract InterestType getType();

    @JsonProperty("interestRate")
    abstract BigDecimal getRate();

    @JsonUnwrapped
    abstract InterestBreakdown getInterestBreakdown();

    @JsonProperty("interestReason")
    abstract String getReason();

    @JsonProperty("interestSpecificDailyAmount")
    abstract BigDecimal getSpecificDailyAmount();

    @JsonUnwrapped
    abstract InterestDate getInterestDate();
}
