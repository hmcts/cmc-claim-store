package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.LinkDto;
import uk.gov.hmcts.reform.payments.client.models.LinksDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentRequest;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment.PAYMENT_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class PaymentsServiceTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String RETURN_URL = "http://returnUrl.test";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String CURRENCY = "currency";
    private static final String DESCRIPTION = "description";
    private static final OffsetDateTime PAYMENT_DATE = OffsetDateTime.parse("2017-02-03T10:15:30+01:00");
    private static final String NEXT_URL = "http://url.test";

    private PaymentsService paymentsService;

    @Mock
    private PaymentsClient paymentsClient;
    @Mock
    private FeesClient feesClient;
    @Spy
    private PaymentDto paymentDto = PaymentDto.builder()
        .status("Success")
        .dateCreated(PAYMENT_DATE)
        .links(LinksDto.builder().nextUrl(
            LinkDto.builder().href(URI.create(NEXT_URL)).build())
            .build())
        .build();

    private Claim claim;
    private final FeeLookupResponseDto feeOutcome = FeeLookupResponseDto.builder()
        .feeAmount(BigDecimal.TEN)
        .build();

    @Before
    public void setUp() {
        paymentsService = new PaymentsService(
            paymentsClient,
            feesClient,
            CURRENCY,
            DESCRIPTION
        );
        claim = SampleClaim.getDefault();
        when(feesClient.lookupFee(eq("online"), eq("issue"), any(BigDecimal.class)))
            .thenReturn(feeOutcome);
    }

    @Test
    public void shouldRetrieveAnExistingPayment() {
        when(paymentsClient.retrieveCardPayment(
            BEARER_TOKEN,
            PAYMENT_REFERENCE
        )).thenReturn(paymentDto);

        Payment expectedPayment = Payment.builder()
            .status(PaymentStatus.SUCCESS)
            .nextUrl(NEXT_URL)
            .returnUrl(RETURN_URL)
            .dateCreated(PAYMENT_DATE.toLocalDate().toString())
            .build();

        Optional<Payment> payment = paymentsService.retrievePayment(
            BEARER_TOKEN,
            claim.getClaimData()
        );

        assertThat(payment).contains(expectedPayment);
    }

    @Test
    public void shouldRetrieveAnExistingPaymentWithNoNextUrl() {
        PaymentDto retrievedPayment = PaymentDto.builder()
            .status("Success")
            .dateCreated(PAYMENT_DATE)
            .links(LinksDto.builder().nextUrl(null).build())
            .build();
        when(paymentsClient.retrieveCardPayment(
            BEARER_TOKEN,
            PAYMENT_REFERENCE
        )).thenReturn(retrievedPayment);

        Payment expectedPayment = Payment.builder()
            .status(PaymentStatus.SUCCESS)
            .nextUrl(null)
            .returnUrl(RETURN_URL)
            .dateCreated(PAYMENT_DATE.toLocalDate().toString())
            .build();

        Optional<Payment> payment = paymentsService.retrievePayment(
            BEARER_TOKEN,
            claim.getClaimData()
        );

        assertThat(payment).contains(expectedPayment);
    }

    @Test
    public void shouldRetrieveAnExistingPaymentWithCreatedDate() {
        PaymentDto retrievedPayment = PaymentDto.builder()
            .status("Success")
            .dateCreated(null)
            .links(LinksDto.builder().nextUrl(
                LinkDto.builder().href(URI.create(NEXT_URL)).build())
                .build())
            .build();
        when(paymentsClient.retrieveCardPayment(
            BEARER_TOKEN,
            PAYMENT_REFERENCE
        )).thenReturn(retrievedPayment);

        Payment expectedPayment = Payment.builder()
            .status(PaymentStatus.SUCCESS)
            .nextUrl(NEXT_URL)
            .dateCreated(claim.getClaimData().getPayment().map(Payment::getDateCreated).orElse(null))
            .returnUrl(RETURN_URL)
            .build();

        Optional<Payment> payment = paymentsService.retrievePayment(
            BEARER_TOKEN,
            claim.getClaimData()
        );

        assertThat(payment).contains(expectedPayment);
    }

    @Test
    public void shouldReturnEmptyWhenPaymentIsNotPresent() {
        assertThat(paymentsService.retrievePayment(
            BEARER_TOKEN,
            SampleClaimData.builder().withPayment(null).build()
        )).isEmpty();
    }

    @Test
    public void shouldMakePaymentAndSetThePaymentAmount() {
        FeeDto[] fees = new FeeDto[]{
            FeeDto.builder()
                .ccdCaseNumber(String.valueOf(claim.getCcdCaseId()))
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(String.valueOf(feeOutcome.getVersion()))
                .build()
        };

        CardPaymentRequest expectedPaymentRequest =
            CardPaymentRequest.builder()
                .caseType(CASE_TYPE_ID)
                .description(DESCRIPTION)
                .currency(CURRENCY)
                .fees(fees)
                .amount(feeOutcome.getFeeAmount())
                .ccdCaseNumber(String.valueOf(claim.getCcdCaseId()))
                .caseReference(claim.getExternalId())
                .build();

        when(paymentsClient.createCardPayment(
            BEARER_TOKEN,
            expectedPaymentRequest,
            RETURN_URL,
            ""
        )).thenReturn(paymentDto);

        paymentsService.createPayment(
            BEARER_TOKEN,
            claim
        );

        verify(paymentDto).setAmount(BigDecimal.TEN);
    }

    @Test
    public void shouldRetrieveAnExistingPaymentWithTransactionId() {
        String externalReference = "External Reference";
        PaymentDto retrievedPayment = PaymentDto.builder()
            .status("Success")
            .dateCreated(null)
            .externalReference(externalReference)
            .links(LinksDto.builder().nextUrl(
                LinkDto.builder().href(URI.create(NEXT_URL)).build())
                .build())
            .build();
        when(paymentsClient.retrieveCardPayment(
            BEARER_TOKEN,
            PAYMENT_REFERENCE
        )).thenReturn(retrievedPayment);

        Payment expectedPayment = Payment.builder()
            .status(PaymentStatus.SUCCESS)
            .nextUrl(NEXT_URL)
            .returnUrl(RETURN_URL)
            .dateCreated(claim.getClaimData().getPayment().map(Payment::getDateCreated).orElse(null))
            .transactionId(externalReference)
            .build();

        Optional<Payment> payment = paymentsService.retrievePayment(
            BEARER_TOKEN,
            claim.getClaimData()
        );

        assertThat(payment).contains(expectedPayment);
    }

    @Test
    public void shouldRetrieveAnExistingPaymentWithFeeId() {
        Integer feeId = 999;

        FeeDto[] fees = {FeeDto.builder().id(feeId).build()};
        PaymentDto retrievedPayment = PaymentDto.builder()
            .status("Success")
            .dateCreated(null)
            .fees(fees)
            .links(LinksDto.builder().nextUrl(
                LinkDto.builder().href(URI.create(NEXT_URL)).build())
                .build())
            .build();
        when(paymentsClient.retrieveCardPayment(
            BEARER_TOKEN,
            PAYMENT_REFERENCE
        )).thenReturn(retrievedPayment);

        Payment expectedPayment = Payment.builder()
            .status(PaymentStatus.SUCCESS)
            .nextUrl(NEXT_URL)
            .returnUrl(RETURN_URL)
            .dateCreated(claim.getClaimData().getPayment().map(Payment::getDateCreated).orElse(null))
            .feeId(feeId.toString())
            .build();

        Optional<Payment> payment = paymentsService.retrievePayment(
            BEARER_TOKEN,
            claim.getClaimData()
        );

        assertThat(payment).contains(expectedPayment);
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
    public void shouldBubbleUpExceptionIfPaymentCreationFails() {cancelCardPayment
        when(paymentsClient.createCardPayment(
            eq(BEARER_TOKEN),
            any(CardPaymentRequest.class),
            anyString(),
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
