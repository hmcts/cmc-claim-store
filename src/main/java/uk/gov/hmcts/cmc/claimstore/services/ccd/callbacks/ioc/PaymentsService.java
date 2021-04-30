package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.client.request.CardPaymentRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Conditional(FeesAndPaymentsConfiguration.class)
public class PaymentsService {
    private static final String FEE_CHANNEL = "online";
    private static final String FEE_EVENT = "issue";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentsClient paymentsClient;
    private final FeesClient feesClient;
    private final String service;
    private final String siteId;
    private final String currency;
    private final String description;

    public PaymentsService(
        PaymentsClient paymentsClient,
        FeesClient feesClient,
        @Value("${payments.api.service}") String service,
        @Value("${payments.api.siteId}") String siteId,
        @Value("${payments.api.currency}") String currency,
        @Value("${payments.api.description}") String description
    ) {
        this.paymentsClient = paymentsClient;
        this.feesClient = feesClient;
        this.service = service;
        this.siteId = siteId;
        this.currency = currency;
        this.description = description;
    }

    public Optional<Payment> retrievePayment(
        String authorisation,
        ClaimData claimData
    ) {

        Optional<Payment> payment = claimData.getPayment();
        if (!payment.isPresent()) {
            return Optional.empty();
        }

        Payment claimPayment = payment.get();
        logger.info("Retrieving payment with reference {}", claimPayment.getReference());

        PaymentDto paymentDto = paymentsClient.retrieveCardPayment(authorisation, claimPayment.getReference());
        return Optional.of(from(paymentDto, claimPayment));
    }

    public Payment createPayment(
        String authorisation,
        Claim claim
    ) {

        logger.info("Creating payment in pay hub for claim with external id {}",
            claim.getExternalId());
        Payment claimPayment = claim.getClaimData().getPayment().orElseThrow(IllegalStateException::new);
        logger.info("Return URL: {}", claimPayment.getReturnUrl());
        logger.info("Return URL: {}", claimPayment.getReturnUrl().contains("test"));
        String serviceCallBackUrl = null;

        if (claimPayment.getReturnUrl().contains("test")
            || claimPayment.getReturnUrl().contains("localhost")) {
            serviceCallBackUrl = claimPayment.getReturnUrl()
                + "/payment/payment-update";
        } else {
            String env = claimPayment.getReturnUrl().substring(claimPayment.getReturnUrl()
                .indexOf(".") + 1, claimPayment.getReturnUrl().lastIndexOf(".platform."));
            serviceCallBackUrl = "http://cmc-claim-store-"
                + env
                + ".service.core-compute-"
                + env
                + ".internal"
                + "/payment/payment-update";
        }

        logger.info("Service Callback URL: {}", serviceCallBackUrl);

        logger.info("Calculating interest amount for claim with external id {}", claim.getExternalId());

        BigDecimal amount = claim.getTotalClaimAmount()
            .orElseThrow(() -> new IllegalStateException("Missing total claim amount"));
        BigDecimal interest = claim.getTotalInterest().orElse(BigDecimal.ZERO);

        BigDecimal amountPlusInterest = amount.add(interest);

        logger.info("Retrieving fee for claim with external id {}",
            claim.getExternalId());

        FeeLookupResponseDto feeOutcome = feesClient.lookupFee(
            FEE_CHANNEL, FEE_EVENT, amountPlusInterest
        );

        CardPaymentRequest paymentRequest = buildPaymentRequest(
            claim,
            feeOutcome
        );
        PaymentDto payment = paymentsClient.createCardPayment(
            authorisation,
            paymentRequest,
            claimPayment.getReturnUrl(),
            serviceCallBackUrl
        );
        logger.info("Created payment for claim with external id {}", claim.getExternalId());

        payment.setAmount(feeOutcome.getFeeAmount());
        return from(payment, claimPayment);
    }

    public void cancelPayment(String authorisation, String paymentReference) {
        logger.info("Cancelling payment {}", paymentReference);
        paymentsClient.cancelCardPayment(authorisation, paymentReference);
    }

    private FeeDto[] buildFees(String ccdCaseId, FeeLookupResponseDto feeOutcome) {
        return new FeeDto[]{
            FeeDto.builder()
                .ccdCaseNumber(ccdCaseId)
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(String.valueOf(feeOutcome.getVersion()))
                .build()
        };
    }

    private CardPaymentRequest buildPaymentRequest(
        Claim claim,
        FeeLookupResponseDto feeOutcome
    ) {
        String ccdCaseId = String.valueOf(claim.getCcdCaseId());
        FeeDto[] fees = buildFees(ccdCaseId, feeOutcome);
        return CardPaymentRequest.builder()
            .caseReference(claim.getExternalId())
            .ccdCaseNumber(ccdCaseId)
            .amount(feeOutcome.getFeeAmount())
            .fees(fees)
            .service(service)
            .currency(currency)
            .description(description)
            .siteId(siteId)
            .build();
    }

    private Payment from(PaymentDto paymentDto, Payment claimPayment) {
        String dateCreated = Optional.ofNullable(paymentDto.getDateCreated())
            .map(date -> date.toLocalDate().toString())
            .orElse(claimPayment.getDateCreated());

        String nextUrl = Optional.ofNullable(paymentDto.getLinks().getNextUrl())
            .map(url -> url.getHref().toString())
            .orElse(claimPayment.getNextUrl());

        String feeId = null;
        if (paymentDto.getFees() != null) {
            feeId = Arrays.stream(paymentDto.getFees())
                .map(FeeDto::getId)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        }

        return Payment.builder()
            .amount(paymentDto.getAmount())
            .reference(paymentDto.getReference())
            .status(PaymentStatus.fromValue(paymentDto.getStatus()))
            .dateCreated(dateCreated)
            .nextUrl(nextUrl)
            .returnUrl(claimPayment.getReturnUrl())
            .serviceCallbackUrl(claimPayment.getServiceCallbackUrl())
            .transactionId(paymentDto.getExternalReference())
            .feeId(feeId)
            .build();
    }
}
