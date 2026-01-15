package uk.gov.hmcts.cmc.claimstore.services.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.LinkDto;
import uk.gov.hmcts.reform.payments.client.models.LinksDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentsClientStub extends PaymentsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsClientStub.class);

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
            .status(PaymentStatus.SUCCESS.getStatus())
            .externalReference(UUID.randomUUID().toString())
            .links(buildLinks(nextUrl))
            .dateCreated(OffsetDateTime.now())
            .fees(withFeeIds(paymentRequest.getFees()))
            .caseReference(paymentRequest.getCaseReference())
            .ccdCaseNumber(paymentRequest.getCcdCaseNumber())
            .currency(paymentRequest.getCurrency())
            .description(paymentRequest.getDescription())
            .build();

        payment.setAmount(paymentRequest.getAmount());
        payments.put(payment.getReference(), payment);
        LOGGER.info(
            "Created stub payment with reference {} for case {}",
            payment.getReference(),
            paymentRequest.getCaseReference()
        );
        return payment;
    }

    @Override
    public PaymentDto retrieveCardPayment(String authorisation, String paymentReference) {
        PaymentDto payment = payments.get(paymentReference);
        if (payment == null) {
            LOGGER.warn("Requested stub payment {} but no payment exists", paymentReference);
            throw new IllegalStateException("No stubbed payment with reference " + paymentReference);
        }
        LOGGER.info("Retrieved stub payment {}", paymentReference);
        return payment;
    }

    @Override
    public void cancelCardPayment(String authorisation, String paymentReference) {
        PaymentDto payment = payments.get(paymentReference);
        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED.getStatus());
            LOGGER.info("Marked stub payment {} as failed", paymentReference);
        } else {
            LOGGER.info("Cancel requested for stub payment {} but none exists", paymentReference);
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

    private FeeDto[] withFeeIds(FeeDto[] requestFees) {
        if (requestFees == null) {
            return null;
        }

        AtomicInteger counter = new AtomicInteger(1);
        return Arrays.stream(requestFees)
            .map(fee -> {
                if (fee == null) {
                    return null;
                }
                return FeeDto.builder()
                    .id(counter.getAndIncrement())
                    .ccdCaseNumber(fee.getCcdCaseNumber())
                    .calculatedAmount(fee.getCalculatedAmount())
                    .code(fee.getCode())
                    .version(fee.getVersion())
                    .build();
            })
            .toArray(FeeDto[]::new);
    }

    private String generateReference() {
        return "RC-" + UUID.randomUUID();
    }
}
