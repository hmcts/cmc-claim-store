package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.amount.AmountType;

import java.math.BigDecimal;

public interface AmountRangeMixIn {

    @JsonProperty("type")
    AmountType getType();

    @JsonProperty("amountLowerValue")
    BigDecimal getLowerValue();

    @JsonProperty("amountHigherValue")
    BigDecimal getHigherValue();

}
