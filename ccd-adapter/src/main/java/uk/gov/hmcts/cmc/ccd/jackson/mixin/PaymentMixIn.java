package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public interface PaymentMixIn {

    @JsonProperty("paymentAmount")
    BigDecimal getAmount();

    @JsonProperty("paymentId")
    String getId();

    @JsonProperty("paymentReference")
    String getReference();

    @JsonProperty("paymentStatus")
    String getStatus();

    @JsonProperty("paymentDateCreated")
    String getDateCreated();
}
