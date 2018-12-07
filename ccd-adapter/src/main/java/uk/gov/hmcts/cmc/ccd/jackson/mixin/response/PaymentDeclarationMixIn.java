package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface PaymentDeclarationMixIn {

    @JsonProperty("paymentDeclarationPaidDate")
    LocalDate getPaidDate();

    @JsonProperty("paymentDeclarationExplanation")
    String getExplanation();
}
