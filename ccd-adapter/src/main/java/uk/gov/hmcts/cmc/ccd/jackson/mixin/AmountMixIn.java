package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AmountMixIn {

    @JsonProperty("amountType")
    abstract String getType();
}
