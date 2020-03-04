package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.CardPaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT;

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
    private final String returnUrlPattern;

    public PaymentsService(
        PaymentsClient paymentsClient,
        FeesClient feesClient,
        @Value("${payments.returnUrlPattern}") String returnUrlPattern,
        @Value("${payments.api.service}") String service,
        @Value("${payments.api.siteId}") String siteId,
        @Value("${payments.api.currency}") String currency,
        @Value("${payments.api.description}") String description
    ) {
        this.paymentsClient = paymentsClient;
        this.feesClient = feesClient;
        this.returnUrlPattern = returnUrlPattern;
        this.service = service;
        this.siteId = siteId;
        this.currency = currency;
        this.description = description;
    }

    public Payment retrievePayment(
        String authorisation,
        Claim claim
    ) {

        logger.info("Retrieving payment amount for claim with external id {}",
            claim.getExternalId());

        Payment claimPayment = claim.getClaimData().getPayment()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT));

        return from(paymentsClient.retrievePayment(authorisation, claimPayment.getReference()),
            claimPayment.getNextUrl()
        );
    }

    public Payment createPayment(
        String authorisation,
        Claim claim
    ) {

        logger.info("Calculating interest amount for claim with external id {}",
            claim.getExternalId());

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

        logger.info("Creating payment in pay hub for claim with external id {}",
            claim.getExternalId());
        logger.info("Next URL: {}", format(returnUrlPattern, claim.getExternalId()));
        PaymentDto payment = paymentsClient.createPayment(
            authorisation,
            paymentRequest,
            format(returnUrlPattern, claim.getExternalId())
        );

        payment.setAmount(feeOutcome.getFeeAmount());
        return from(payment, null);
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

    private Payment from(PaymentDto paymentDto, String nextUrlCurrent) {
        String dateCreated = Optional.ofNullable(paymentDto.getDateCreated())
            .map(date -> date.toLocalDate().toString())
            .orElse(null);
        String nextUrl = Optional.ofNullable(paymentDto.getLinks().getNextUrl())
            .map(url -> url.getHref().toString())
            .orElse(nextUrlCurrent);
        return Payment.builder()
            .amount(paymentDto.getAmount())
            .reference(paymentDto.getReference())
            .status(PaymentStatus.fromValue(paymentDto.getStatus()))
            .dateCreated(dateCreated)
            .nextUrl(nextUrl)
            .build();
    }
}
