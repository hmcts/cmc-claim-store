package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ResetRpaCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RoboticsNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class ResetRpaCallbackHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String EXTERNAL_ID = "external id";
    private static final String CASE_NAME = "case name";
    private static final String REFERENCE = "reference";
    private static final String REFERENCE_KEY = "previousServiceCaseReference";
    private static final Long ID = 1L;
    private static final String EVENT_ID = "ResetRPA";
    private static final String RPA_EVENT_TYPE_KEY = "RPAEventType";
    private static final String CLAIM = "CLAIM";
    private static final String MORE_TIME = "MORE_TIME";
    private static final String CCJ = "CCJ";
    private static final String DEFENDANT_RESPONSE = "DEFENDANT_RESPONSE";
    private static final String PAID_IN_FULL = "PAID_IN_FULL";
    private static final String BREATHING_SPACE_ENTERED = "BREATHING_SPACE_ENTERED";
    private static final String BREATHING_SPACE_LIFTED = "BREATHING_SPACE_LIFTED";
    private static final String INVALID = "INVALID";
    public static final UUID RAND_UUID = UUID.randomUUID();
    private CallbackRequest callbackRequest;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private RoboticsNotificationService roboticsNotificationService;

    private ResetRpaCallbackHandler resetRpaCallbackHandler;

    @Before
    public void setup() {
        resetRpaCallbackHandler = new ResetRpaCallbackHandler(caseDetailsConverter,
            caseMapper, roboticsNotificationService);
        when(caseMapper.to(any(Claim.class))).thenReturn(getCcdCase());
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(getCcdCaseMap());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getClaim());
    }

    @Test
    public void shouldResetRpaWhenValidClaimEventSent() {
        when(roboticsNotificationService.rpaClaimNotification(anyString())).thenReturn(REFERENCE);
        callbackRequest = getCallbackRequest(CLAIM);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaClaimNotification(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidMoreTimeEventSent() {
        callbackRequest = getCallbackRequest(MORE_TIME);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaMoreTimeNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidCcjEventSent() {
        callbackRequest = getCallbackRequest(CCJ);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaCCJNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidDefendantResponseEventSent() {
        callbackRequest = getCallbackRequest(DEFENDANT_RESPONSE);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaResponseNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenValidPaidInFullEventSent() {
        callbackRequest = getCallbackRequest(PAID_IN_FULL);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaPIFNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenBreathingSpaceEnteredSuccessfully() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getDefaultWithBreathingSpaceEnteredDetails());

        callbackRequest = getCallbackRequest(BREATHING_SPACE_ENTERED);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaEnterBreathingSpaceNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaInvokedWhenBreathingSpaceNotEntered() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getDefault());

        callbackRequest = getCallbackRequest(BREATHING_SPACE_ENTERED);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assert(response.getErrors().get(0).contains("This claim is still not entered into Breathing space"));
        //verify(roboticsNotificationService).rpaEnterBreathingSpaceNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaWhenBreathingSpaceLiftedSuccessfully() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getDefaultWithBreathingSpaceLiftedDetails());

        callbackRequest = getCallbackRequest(BREATHING_SPACE_LIFTED);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        MatcherAssert.assertThat(response.getData().get(REFERENCE_KEY), CoreMatchers.is(REFERENCE));
        verify(roboticsNotificationService).rpaLiftBreathingSpaceNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaInvokedWhenBreathingSpaceNotEnteredAndInvokedBreathingSpaceLifted() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getDefault());

        callbackRequest = getCallbackRequest(BREATHING_SPACE_LIFTED);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assert(response.getErrors().get(0).contains("This claim is still not entered into Breathing space"));
       // verify(roboticsNotificationService).rpaLiftBreathingSpaceNotifications(eq(REFERENCE));
    }

    @Test
    public void shouldResetRpaInvokedWhenBreathingSpaceEnteredAndInvokedBreathingSpaceLifted() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(getDefaultWithBreathingSpaceEnteredDetails());

        callbackRequest = getCallbackRequest(BREATHING_SPACE_LIFTED);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
        assert(response.getErrors().get(0).contains("This claim is still not lifted its Breathing space"));
        // verify(roboticsNotificationService).rpaLiftBreathingSpaceNotifications(eq(REFERENCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidEventSent() {
        callbackRequest = getCallbackRequest(INVALID);
        resetRpaCallbackHandler.handle(getCallbackParams(callbackRequest));
    }

    @Test
    public void getSupportedRoles() {
        List<Role> ROLES = Collections.singletonList(CASEWORKER);
        List<Role> roleList = resetRpaCallbackHandler.getSupportedRoles();
        assertEquals(ROLES, roleList);
    }

    @Test
    public void handledEvents() {
        List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.RESEND_RPA);
        List<CaseEvent> caseEventList = resetRpaCallbackHandler.handledEvents();
        assertEquals(EVENTS, caseEventList);
    }

    private CCDCase getCcdCase() {
        return CCDCase.builder()
            .externalId(EXTERNAL_ID)
            .previousServiceCaseReference(REFERENCE)
            .caseName(CASE_NAME)
            .id(ID)
            .build();
    }

    private Map<String, Object> getCcdCaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(EXTERNAL_ID, getCcdCase());
        map.put(REFERENCE_KEY, REFERENCE);
        return map;
    }

    private Claim getClaim() {
        return Claim.builder()
            .referenceNumber(REFERENCE)
            .externalId(EXTERNAL_ID)
            .build();
    }

    private CallbackRequest getCallbackRequest(String rpaEventType) {
        Map<String, Object> data = new HashMap<>();
        data.put(RPA_EVENT_TYPE_KEY, rpaEventType);
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(data).build())
            .eventId(EVENT_ID)
            .build();
    }

    private CallbackParams getCallbackParams(CallbackRequest callbackRequest) {
        return CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    public static Claim getDefault() {
        return SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(RAND_UUID)
                .withInterest(new SampleInterest()
                    .withType(Interest.InterestType.BREAKDOWN)
                    .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                    .withRate(BigDecimal.valueOf(8))
                    .withReason("Need flat rate")
                    .build())
                .withPayment(SamplePayment.builder()
                    .status(PaymentStatus.FAILED)
                    .build())
                .build())
            .withReferenceNumber(REFERENCE)
            .withResponseDeadline(null)
            .build();
    }

    public static Claim getDefaultWithBreathingSpaceEnteredDetails() {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "No");
        return SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimantBuilder().withExternalId(RAND_UUID)
                .withBreathingSpace(breathingSpace).build())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .ccjType(CountyCourtJudgmentType.ADMISSIONS)
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(YES)
                .build()
            ).withState(ClaimState.OPEN)
            .withReferenceNumber(REFERENCE)
            .build();
    }

    public static Claim getDefaultWithBreathingSpaceLiftedDetails() {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "Yes");
        return SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimantBuilder().withExternalId(RAND_UUID)
                .withBreathingSpace(breathingSpace).build())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .ccjType(CountyCourtJudgmentType.ADMISSIONS)
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(YES)
                .build()
            ).withState(ClaimState.OPEN)
            .withReferenceNumber(REFERENCE)
            .build();
    }
}
