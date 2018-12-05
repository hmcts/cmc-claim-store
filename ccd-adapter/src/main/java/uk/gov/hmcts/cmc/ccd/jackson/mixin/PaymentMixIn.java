package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public abstract class PaymentMixIn {

    @JsonProperty("paymentAmount")
    abstract BigDecimal getAmount();

    @JsonProperty("paymentId")
    abstract String getId();

    @JsonProperty("paymentReference")
    abstract String getReference();

    @JsonProperty("paymentStatus")
    abstract String getStatus();

    @JsonProperty("paymentDateCreated")
    abstract String getDateCreated();
}
