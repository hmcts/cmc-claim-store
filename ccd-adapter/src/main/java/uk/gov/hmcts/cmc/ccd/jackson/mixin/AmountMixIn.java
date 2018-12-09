package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.amount.AmountType;

@SuppressWarnings("squid:S1610")
public abstract class AmountMixIn {
    private AmountType type;

    public AmountMixIn(@JsonProperty("amountType") AmountType type) {
        this.type = type;
    }
}
