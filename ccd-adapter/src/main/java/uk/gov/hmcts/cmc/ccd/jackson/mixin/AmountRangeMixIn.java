package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Optional;

public abstract class AmountRangeMixIn extends AmountMixIn {

    public AmountRangeMixIn(
        @JsonProperty("amountLowerValue") BigDecimal lowerValue,
        @JsonProperty("amountHigherValue") BigDecimal higherValue
    ) {
    }
}
