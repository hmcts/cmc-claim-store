package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.AmountType;

import java.util.List;

public abstract class AmountMixIn {

    @JsonProperty("type")
    abstract AmountType getType();
}
