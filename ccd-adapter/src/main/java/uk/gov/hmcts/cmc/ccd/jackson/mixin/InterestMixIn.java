package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest.InterestType;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;

public interface InterestMixIn {

    @JsonProperty("interestType")
     InterestType getType();

    @JsonProperty("interestRate")
     BigDecimal getRate();

    @JsonUnwrapped
     InterestBreakdown getInterestBreakdown();

    @JsonProperty("interestReason")
     String getReason();

    @JsonProperty("interestSpecificDailyAmount")
     BigDecimal getSpecificDailyAmount();

    @JsonUnwrapped
     InterestDate getInterestDate();
}
