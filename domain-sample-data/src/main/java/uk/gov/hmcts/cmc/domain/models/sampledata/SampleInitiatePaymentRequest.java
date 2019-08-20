package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;

import java.time.LocalDate;
import java.util.UUID;

public class SampleInitiatePaymentRequest {

    private SampleInitiatePaymentRequest() {
        super();
    }

    public static InitiatePaymentRequest.InitiatePaymentRequestBuilder builder() {
        return InitiatePaymentRequest.builder()
            .issuedOn(LocalDate.parse("2020-02-06"))
            .interest(SampleInterest.standard())
            .amount(SampleAmountBreakdown.builder().build())
            .externalId(UUID.fromString("acd82549-d279-4adc-b38c-d195dd0db0d6"));

    }
}
