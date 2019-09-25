package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.CardPaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentsServiceTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String RETURN_URL = "http://returnUrl.test/blah/%s/test";
    private static final String SERVICE = "CMC";
    private static final String SITE_ID = "siteId";
    private static final String CURRENCY = "currency";
    private static final String DESCRIPTION = "description";

    private PaymentsService paymentsService;

    @Mock
    private PaymentsClient paymentsClient;
    @Mock
    private FeesClient feesClient;
    @Mock
    private PaymentDto paymentDto;

    private Claim claim;
    private FeeLookupResponseDto feeOutcome = FeeLookupResponseDto.builder()
        .feeAmount(BigDecimal.TEN)
        .build();

    @Before
    public void setUp() {
        paymentsService = new PaymentsService(
            paymentsClient,
            feesClient,
            RETURN_URL,
            SERVICE,
            SITE_ID,
            CURRENCY,
            DESCRIPTION
        );
        claim = SampleClaim.getDefault();
        when(feesClient.lookupFee(eq("online"), eq("issue"), any(BigDecimal.class)))
            .thenReturn(feeOutcome);
    }

    @Test
    public void shouldMakePaymentAndSetThePaymentAmount() {
        FeeDto[] fees = new FeeDto[] {
            FeeDto.builder()
                .ccdCaseNumber(String.valueOf(claim.getCcdCaseId()))
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(String.valueOf(feeOutcome.getVersion()))
                .build()
        };

        CardPaymentRequest expectedPaymentRequest =
            CardPaymentRequest.builder()
                .siteId(SITE_ID)
                .description(DESCRIPTION)
                .currency(CURRENCY)
                .service(SERVICE)
                .fees(fees)
                .amount(new BigDecimal("51.91"))
                .ccdCaseNumber(String.valueOf(claim.getCcdCaseId()))
                .caseReference(claim.getExternalId())
                .build();

        when(paymentsClient.createPayment(
            BEARER_TOKEN,
            expectedPaymentRequest,
            format(RETURN_URL, claim.getExternalId())
        )).thenReturn(paymentDto);

        paymentsService.createPayment(
            BEARER_TOKEN,
            claim
        );

        verify(paymentDto).setAmount(new BigDecimal("51.91"));
    }

    @Test
    public void shouldMakePaymentAndSetThePaymentAmountWithNoInterest() {
        ClaimData claimData = SampleClaimData.noInterest();
        Claim claimWithNoInterest = SampleClaim.builder().withClaimData(claimData).build();
        FeeDto[] fees = new FeeDto[] {
            FeeDto.builder()
                .ccdCaseNumber(String.valueOf(claimWithNoInterest.getCcdCaseId()))
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(String.valueOf(feeOutcome.getVersion()))
                .build()
        };

        CardPaymentRequest expectedPaymentRequest =
            CardPaymentRequest.builder()
                .siteId(SITE_ID)
                .description(DESCRIPTION)
                .currency(CURRENCY)
                .service(SERVICE)
                .fees(fees)
                .amount(new BigDecimal("50.99"))
                .ccdCaseNumber(String.valueOf(claimWithNoInterest.getCcdCaseId()))
                .caseReference(claimWithNoInterest.getExternalId())
                .build();

        when(paymentsClient.createPayment(
            BEARER_TOKEN,
            expectedPaymentRequest,
            format(RETURN_URL, claimWithNoInterest.getExternalId())
        )).thenReturn(paymentDto);

        paymentsService.createPayment(
            BEARER_TOKEN,
            claimWithNoInterest
        );

        verify(paymentDto).setAmount(new BigDecimal("50.99"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleUpExceptionIfFeeLookupFails() {
        when(feesClient.lookupFee(eq("online"), eq("issue"), any(BigDecimal.class)))
            .thenThrow(IllegalStateException.class);

        paymentsService.createPayment(
            BEARER_TOKEN,
            claim
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleUpExceptionIfPaymentCreationFails() {
        when(paymentsClient.createPayment(
            eq(BEARER_TOKEN),
            any(CardPaymentRequest.class),
            anyString()))
            .thenThrow(IllegalStateException.class);

        paymentsService.createPayment(
            BEARER_TOKEN,
            claim
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfAmountIsNotCalculated() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withAmount(null)
                .build())
            .build();
        paymentsService.createPayment(
            BEARER_TOKEN,
            claim
        );
    }
}
