package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public abstract class AmountRangeMixIn extends AmountMixIn{

    @JsonProperty("amountLowerValue")
    abstract Optional<BigDecimal> getLowerValue();

    @JsonProperty("amountHigherValue")
    abstract BigDecimal getHigherValue();
}
