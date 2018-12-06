package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public abstract class PaymentDeclarationMixIn {

    @JsonProperty("paymentDeclarationPaidDate")
    abstract LocalDate getPaidDate();

    @JsonProperty("paymentDeclarationExplanation")
    abstract String getExplanation();
}
