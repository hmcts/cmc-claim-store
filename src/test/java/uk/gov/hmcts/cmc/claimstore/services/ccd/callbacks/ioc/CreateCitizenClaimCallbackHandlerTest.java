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
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHwfClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static java.lang.String.format;
import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_HWF_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_HWF_CASE;
import static uk.gov.hmcts.cmc.claimstore.constants.ResponseConstants.CREATE_CLAIM_DISABLED;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.FAILED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.withFullClaimData;

@RunWith(MockitoJUnitRunner.class)
public class CreateCitizenClaimCallbackHandlerTest {
    private static final String REFERENCE_NO = "000MC001";
    private static final String REFERENCE_NO_HWF = "1595459870527766";
    private static final LocalDate ISSUE_DATE = now();
    private static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String NEXT_URL = "http://nexturl.test";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private IssueDateCalculator issueDateCalculator;

    @Mock
    private ReferenceNumberRepository referenceNumberRepository;

    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private PaymentsService paymentsService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private CreateCitizenClaimCallbackHandler createCitizenClaimCallbackHandler;

    private final CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    private Payment.PaymentBuilder paymentBuilder;

    @Before
    public void setUp() {
        paymentBuilder = Payment.builder()
            .amount(TEN)
            .reference("reference2")
            .dateCreated("2017-12-03")
            .nextUrl(NEXT_URL);

        createCitizenClaimCallbackHandler = new CreateCitizenClaimCallbackHandler(
            caseDetailsConverter,
            issueDateCalculator,
            referenceNumberRepository,
            responseDeadlineCalculator,
            caseMapper,
            paymentsService,
            eventProducer,
            userService,
            true
        );

        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();

        when(issueDateCalculator.calculateIssueDay(any())).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(ISSUE_DATE)).thenReturn(RESPONSE_DEADLINE);
        when(referenceNumberRepository.getReferenceNumberForCitizen()).thenReturn(REFERENCE_NO);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenSuccessfulPayment() {
        when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(ClaimData.class)))
            .thenReturn(Optional.of(paymentBuilder.status(SUCCESS).build()));

        Claim claim = SampleClaim.getDefault().toBuilder()
            .referenceNumber(referenceNumberRepository.getReferenceNumberForCitizen())
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getIssuedOn()).contains(ISSUE_DATE);
        assertThat(toBeSaved.getServiceDate()).isEqualTo(ISSUE_DATE.plusDays(5));
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO);
        assertThat(toBeSaved.getResponseDeadline()).isEqualTo(RESPONSE_DEADLINE);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenClaimIsHwfPending() {
        caseDetails.setId(Long.valueOf(REFERENCE_NO_HWF));
        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_HWF_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        Claim claim = SampleHwfClaim.getDefaultHwfPending().toBuilder()
            .id(Long.valueOf(REFERENCE_NO_HWF))
            .referenceNumber(REFERENCE_NO_HWF)
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO_HWF);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenClaimIsInvalidHwfReference() {
        caseDetails.setId(Long.valueOf(REFERENCE_NO_HWF));
        callbackRequest = CallbackRequest.builder()
            .eventId(INVALID_HWF_REFERENCE.getValue())
            .caseDetails(caseDetails)
            .build();

        Claim claim = SampleHwfClaim.getDefaultHwfPending().toBuilder()
            .id(Long.valueOf(REFERENCE_NO_HWF))
            .referenceNumber(REFERENCE_NO_HWF)
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO_HWF);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenClaimIsHwfAwaitingResponse() {
        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_HWF_CASE.getValue())
            .caseDetails(caseDetails)
            .build();
        Claim claim = SampleHwfClaim.getDefaultAwaitingResponseHwf().toBuilder()
            .id(Long.valueOf(REFERENCE_NO_HWF))
            .referenceNumber(REFERENCE_NO_HWF)
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO_HWF);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenUnSuccessfulPayment() {
        when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(ClaimData.class)))
            .thenReturn(Optional.of(paymentBuilder.status(FAILED).build()));

        Claim claim = SampleClaim.withFullClaimDataAndFailedPayment();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            createCitizenClaimCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors()).containsOnly("Payment not successful");
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseForPostOperations() {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(withFullClaimData().getClaimData())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        when(userService.getUser(BEARER_TOKEN))
            .thenReturn(new User(BEARER_TOKEN, SampleUserDetails.builder()
                .withRoles("letter-" + claim.getLetterHolderId())
                .build()));

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(eventProducer, once()).createClaimCreatedEvent(
            claimArgumentCaptor.capture(),
            eq("Steven Smith"),
            eq(BEARER_TOKEN));

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getClaimData()).isEqualTo(claim.getClaimData());
    }

    @Test
    public void shouldThrowExceptionIfMissingPayment() {
        when(paymentsService.retrievePayment(eq(BEARER_TOKEN), any(ClaimData.class)))
            .thenReturn(Optional.empty());

        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(withFullClaimData().getClaimData().toBuilder().payment(null).build())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        assertThatThrownBy(() -> createCitizenClaimCallbackHandler.handle(callbackParams))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(format("Claim with external id %s has no payment record", claim.getExternalId()));

        verify(eventProducer, never()).createClaimCreatedEvent(
            any(Claim.class),
            anyString(),
            anyString());
    }

    @Test
    public void shouldHaveCorrectCitizenRepSupportingRole() {
        assertThat(createCitizenClaimCallbackHandler.getSupportedRoles()).contains(CITIZEN, CASEWORKER);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseWhenHwFClaimIsIssued() {
        callbackRequest = CallbackRequest.builder()
            .eventId(ISSUE_HWF_CASE.getValue())
            .caseDetails(caseDetails)
            .build();
        Claim claim = SampleHwfClaim.getDefaultAwaitingResponseHwf().toBuilder()
            .id(Long.valueOf(REFERENCE_NO_HWF))
            .referenceNumber(REFERENCE_NO_HWF)
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .claimData(withFullClaimData().getClaimData())
            .state(CREATE)
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(caseMapper).to(claimArgumentCaptor.capture());

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getReferenceNumber()).isEqualTo(REFERENCE_NO);
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseForPostOperationsForHwfUpdateEvent() {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(withFullClaimData().getClaimData())
            .state(AWAITING_RESPONSE_HWF)
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        when(userService.getUser(BEARER_TOKEN))
            .thenReturn(new User(BEARER_TOKEN, SampleUserDetails.builder()
                .withRoles("letter-" + claim.getLetterHolderId())
                .build()));

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(eventProducer, once()).createHwfClaimUpdatedEvent(
            claimArgumentCaptor.capture(),
            eq("Steven Smith"),
            eq(BEARER_TOKEN));

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getClaimData()).isEqualTo(claim.getClaimData());
    }

    @Test
    public void shouldSuccessfullyReturnCallBackResponseForPostOperationsForHwfIssueEvent() {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .claimData(withFullClaimData().getClaimData())
            .build();
        callbackRequest = CallbackRequest.builder()
            .eventId(ISSUE_HWF_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(claim);

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        when(userService.getUser(BEARER_TOKEN))
            .thenReturn(new User(BEARER_TOKEN, SampleUserDetails.builder()
                .withRoles("letter-" + claim.getLetterHolderId())
                .build()));

        createCitizenClaimCallbackHandler.handle(callbackParams);

        verify(eventProducer, once()).issueHelpWithFeesClaimEvent(
            claimArgumentCaptor.capture(),
            eq("Steven Smith"),
            eq(BEARER_TOKEN));

        Claim toBeSaved = claimArgumentCaptor.getValue();
        assertThat(toBeSaved.getClaimData()).isEqualTo(claim.getClaimData());
    }

    @Test
    public void shouldThrowExceptionWhenFeatureCreateClaimIsDisabled() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        CreateCitizenClaimCallbackHandler handler = new CreateCitizenClaimCallbackHandler(
            caseDetailsConverter,
            issueDateCalculator,
            referenceNumberRepository,
            responseDeadlineCalculator,
            caseMapper,
            paymentsService,
            eventProducer,
            userService,
            false
        );
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().get(0)).isEqualTo(CREATE_CLAIM_DISABLED);
    }
}
