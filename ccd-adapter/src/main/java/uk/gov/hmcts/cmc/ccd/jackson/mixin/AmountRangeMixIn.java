package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Optional;

public interface AmountRangeMixIn extends AmountMixIn {

    @JsonProperty("amountLowerValue")
     Optional<BigDecimal> getLowerValue();

    @JsonProperty("amountHigherValue")
     BigDecimal getHigherValue();
}
