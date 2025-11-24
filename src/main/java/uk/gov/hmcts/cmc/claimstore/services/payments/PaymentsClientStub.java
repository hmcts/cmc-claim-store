package uk.gov.hmcts.cmc.claimstore.services.payments;

import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.LinkDto;
import uk.gov.hmcts.reform.payments.client.models.LinksDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentsClientStub extends PaymentsClient {

    private final Map<String, PaymentDto> payments = new ConcurrentHashMap<>();

    public PaymentsClientStub() {
        super(null, null);
    }

    @Override
    public PaymentDto createCardPayment(
        String authorisation,
        CardPaymentRequest paymentRequest,
        String returnUrl,
        String nextUrl
    ) {
        PaymentDto payment = PaymentDto.builder()
            .reference(generateReference())
            .status(PaymentStatus.INITIATED.getStatus())
            .externalReference(UUID.randomUUID().toString())
            .links(buildLinks(nextUrl))
            .dateCreated(OffsetDateTime.now())
            .fees(paymentRequest.getFees())
            .caseReference(paymentRequest.getCaseReference())
            .ccdCaseNumber(paymentRequest.getCcdCaseNumber())
            .currency(paymentRequest.getCurrency())
            .description(paymentRequest.getDescription())
            .build();

        payment.setAmount(paymentRequest.getAmount());
        payments.put(payment.getReference(), payment);
        return payment;
    }

    @Override
    public PaymentDto retrieveCardPayment(String authorisation, String paymentReference) {
        PaymentDto payment = payments.get(paymentReference);
        if (payment == null) {
            throw new IllegalStateException("No stubbed payment with reference " + paymentReference);
        }
        return payment;
    }

    @Override
    public void cancelCardPayment(String authorisation, String paymentReference) {
        PaymentDto payment = payments.get(paymentReference);
        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED.getStatus());
        }
    }

    private LinksDto buildLinks(String nextUrl) {
        if (nextUrl == null) {
            return LinksDto.builder().build();
        }
        return LinksDto.builder()
            .nextUrl(LinkDto.builder().href(URI.create(nextUrl)).build())
            .build();
    }

    private String generateReference() {
        return "RC-" + UUID.randomUUID();
    }
}
