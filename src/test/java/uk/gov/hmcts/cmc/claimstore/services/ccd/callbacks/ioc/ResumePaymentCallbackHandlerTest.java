package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.INITIATED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.PENDING;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class ResumePaymentCallbackHandlerTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String NEXT_URL = "http://nexturl.test";
    private static final Long CASE_ID = 3L;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private PaymentsService paymentsService;
    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private CallbackRequest callbackRequest;

    private ResumePaymentCallbackHandler handler;

    @Before
    public void setUp() {
        handler = new ResumePaymentCallbackHandler(
            paymentsService,
            caseDetailsConverter,
            caseMapper,
            issueDateCalculator,
            responseDeadlineCalculator);
        callbackRequest = CallbackRequest
            .builder()
            .eventId(RESUME_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .build())
            .build();
    }

    @Test
    public void shouldUpdateExistingPaymentIfPaymentIsSuccessful() {
        Claim claim = SampleClaim.getDefault();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        Payment originalPayment = Payment.builder()
            .reference("reference")
            .status(SUCCESS)
            .dateCreated("2017-02-03T10:15:30+01:00")
            .nextUrl(NEXT_URL)
            .build();

        when(paymentsService.retrievePayment(
            eq(BEARER_TOKEN),
            any(Claim.class)))
            .thenReturn(originalPayment);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        handler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Payment payment = claimArgumentCaptor.getValue().getClaimData().getPayment().orElse(null);
        assertThat(payment).isEqualTo(originalPayment);
    }

    @Test
    public void shouldUpdateExistingPaymentIfPaymentIsInitiated() {
        Claim claim = SampleClaim.getDefault();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        Payment originalPayment = Payment.builder()
            .reference("reference")
            .status(INITIATED)
            .dateCreated("2017-02-03T10:15:30+01:00")
            .nextUrl(NEXT_URL)
            .build();

        when(paymentsService.retrievePayment(
            eq(BEARER_TOKEN),
            any(Claim.class)))
            .thenReturn(originalPayment);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        handler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Payment payment = claimArgumentCaptor.getValue().getClaimData().getPayment().orElse(null);
        assertThat(payment).isEqualTo(originalPayment);
    }

    @Test
    public void shouldCreatePaymentIfPaymentIsNotInitiatedOrSuccessful() {
        Claim claim = SampleClaim.getDefault();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        Payment originalPayment = Payment.builder()
            .reference("reference")
            .status(PENDING)
            .dateCreated("2017-02-03T10:15:30+01:00")
            .nextUrl(NEXT_URL)
            .build();

        when(paymentsService.retrievePayment(
            eq(BEARER_TOKEN),
            any(Claim.class)))
            .thenReturn(originalPayment);

        Payment newPayment = Payment.builder()
            .reference("reference2")
            .status(SUCCESS)
            .dateCreated("2017-02-03T10:15:30+01:00")
            .nextUrl(NEXT_URL)
            .build();

        when(paymentsService.createPayment(
            eq(BEARER_TOKEN),
            any(Claim.class)))
            .thenReturn(newPayment);

        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        handler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getIssuedOn()).isEqualTo(date);
        assertThat(toBeSaved.getResponseDeadline()).isEqualTo(date);

        Payment payment = toBeSaved.getClaimData().getPayment().orElse(null);
        assertThat(payment).isEqualTo(newPayment);
    }

    @Test
    public void shouldAcceptOnlyCitizenRoles() {
        assertThat(handler.getSupportedRoles())
            .containsOnly(Role.CITIZEN);
    }
}
