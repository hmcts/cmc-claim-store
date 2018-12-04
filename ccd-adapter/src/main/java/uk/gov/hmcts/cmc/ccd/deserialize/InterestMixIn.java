package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class InterestMixIn {

    @JsonProperty("interestType")
    abstract Interest.InterestType getType();

    @JsonProperty("InterestRate")
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
