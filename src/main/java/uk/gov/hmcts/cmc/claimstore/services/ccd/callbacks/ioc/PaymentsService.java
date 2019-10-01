package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.CardPaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static java.lang.String.format;

@Service
public class PaymentsService {
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

    public PaymentDto createPayment(
        String authorisation,
        Claim claim
    ) {

        logger.info("Calculating interest amount for case {}",
            claim.getExternalId());

        logger.info("Retrieving fee for case {}",
            claim.getExternalId());

        BigDecimal amount = claim.getTotalClaimAmount().orElseThrow(IllegalStateException::new);
        BigDecimal interest = claim.getTotalInterest().orElse(BigDecimal.ZERO);

        BigDecimal amountPlusInterest = amount.add(interest);

        FeeLookupResponseDto feeOutcome = feesClient.lookupFee(
            "online", "issue", amountPlusInterest
        );

        BigDecimal totalAmountPlusFees = amountPlusInterest.add(feeOutcome.getFeeAmount());

        CardPaymentRequest paymentRequest = buildPaymentRequest(
            claim,
            feeOutcome,
            totalAmountPlusFees
        );

        PaymentDto payment = paymentsClient.createPayment(
            authorisation,
            paymentRequest,
            format(returnUrlPattern, claim.getExternalId())
        );

        payment.setAmount(totalAmountPlusFees);
        return payment;
    }

    private FeeDto[] buildFees(String ccdCaseId, FeeLookupResponseDto feeOutcome) {
        return new FeeDto[] {
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
        FeeLookupResponseDto feeOutcome,
        BigDecimal amountPlusFees
    ) {
        String ccdCaseId = String.valueOf(claim.getCcdCaseId());
        FeeDto[] fees = buildFees(ccdCaseId, feeOutcome);
        return CardPaymentRequest.builder()
            .caseReference(claim.getExternalId())
            .ccdCaseNumber(ccdCaseId)
            .amount(amountPlusFees)
            .fees(fees)
            .service(service)
            .currency(currency)
            .description(description)
            .siteId(siteId)
            .build();
    }
}
