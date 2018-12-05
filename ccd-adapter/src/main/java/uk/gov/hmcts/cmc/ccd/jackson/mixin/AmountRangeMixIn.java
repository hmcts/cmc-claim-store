package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Optional;

@SuppressWarnings("squid:S1610")
public abstract class AmountRangeMixIn extends AmountMixIn {

    @JsonProperty("amountLowerValue")
    abstract Optional<BigDecimal> getLowerValue();

    @JsonProperty("amountHigherValue")
    abstract BigDecimal getHigherValue();
}
