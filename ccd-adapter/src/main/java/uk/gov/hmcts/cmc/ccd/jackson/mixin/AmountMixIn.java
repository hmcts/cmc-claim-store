package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.amount.AmountType;

public interface AmountMixIn {

    @JsonProperty("amountType")
    String getType();
}
