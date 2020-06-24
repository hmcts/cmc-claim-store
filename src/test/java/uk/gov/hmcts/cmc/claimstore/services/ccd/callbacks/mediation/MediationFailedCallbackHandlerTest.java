package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.JUDGE_PILOT_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.LA_PILOT_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_JUDGE_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_LEGAL_ADVISOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.READY_FOR_TRANSFER;

@RunWith(MockitoJUnitRunner.class)
public class MediationFailedCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private MediationFailedNotificationService mediationFailedNotificationService;

    @Mock
    private AppInsights appInsights;

    @Mock
    private DirectionsQuestionnaireService directionsQuestionnaireService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private MediationFailedCallbackHandler mediationFailedCallbackHandler;

    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String INSTANT_EXPECTED = "2020-06-23T10:10:10";
    private static final String DATE_REFERRED_FOR_DIRECTIONS = "dateReferredForDirections";
    private static final String REFERENCE = "reference";
    private static final String REFERENCE_KEY = "previousServiceCaseReference";

    private final Claim claimSetForMediation =
        SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
    private CallbackRequest callbackRequest;

    //TODO Clean up these tests

    @Before
    public void setUp() {
        mediationFailedCallbackHandler = new MediationFailedCallbackHandler(
            caseDetailsConverter,
            deadlineCalculator,
            caseMapper,
            appInsights,
            mediationFailedNotificationService,
            directionsQuestionnaireService,
            coreCaseDataService);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.MEDIATION_FAILED.getValue())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfNotDefenseOrFullAdmit() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        mediationFailedCallbackHandler.handle(callbackParams);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfClaimantResponseAcceptation() {
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        mediationFailedCallbackHandler.handle(callbackParams);
    }

    @Test
    public void setsStateFirstToOpenForOfflineDQCase() {

        Claim claim = claimSetForMediation.toBuilder()
            .claimData(SampleClaimData.submittedWithAmountMoreThanThousand())
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(deadlineCalculator.calculate(any()))
            .thenReturn(LocalDate.now().plusDays(8));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", OPEN.getValue());

    }

    @Test
    public void shouldSendNotificationsIfOnlineDQCaseSubmitted() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        Claim claim = claimSetForMediation.toBuilder()
            .response(
                SampleResponse
                    .FullDefence
                    .builder()
                    .withDirectionsQuestionnaire(
                        SampleDirectionsQuestionnaire.builder()
                            .withHearingLocation(SampleHearingLocation.pilotHearingLocation)
                            .build()
                    ).build()
            )
            .features(Collections.singletonList("directionsQuestionnaire"))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        mediationFailedCallbackHandler.handle(callbackParams);

        verify(mediationFailedNotificationService).notifyParties(any());
    }

    @Test
    public void setsStateToReadyForTransferIfNotPilotCase() {

        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList(DQ_FLAG.getValue()))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(directionsQuestionnaireService.getDirectionsCaseState(any()))
            .thenReturn(READY_FOR_TRANSFER.getValue());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", READY_FOR_TRANSFER.getValue());

    }

    @Test
    public void setsToReadyForDirectionsIfPilotCase() {

        Claim claim = claimSetForMediation.toBuilder()
            .response(
                SampleResponse
                    .FullDefence
                    .builder()
                    .withDirectionsQuestionnaire(
                        SampleDirectionsQuestionnaire.builder()
                            .withHearingLocation(SampleHearingLocation.pilotHearingLocation)
                            .build()
                    ).build()
            )
            .features(ImmutableList.of(DQ_FLAG.getValue(), LA_PILOT_FLAG.getValue()))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(directionsQuestionnaireService.getDirectionsCaseState(any()))
            .thenReturn(READY_FOR_LEGAL_ADVISOR_DIRECTIONS.getValue());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            mediationFailedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", READY_FOR_LEGAL_ADVISOR_DIRECTIONS.getValue());
    }

    @Test
    public void setsToReadyForJudgeDirectionsIfJudgePilotCase() {

        Claim claim = claimSetForMediation.toBuilder()
            .response(
                SampleResponse
                    .FullDefence
                    .builder()
                    .withDirectionsQuestionnaire(
                        SampleDirectionsQuestionnaire.builder()
                            .withHearingLocation(SampleHearingLocation.pilotHearingLocation)
                            .build()
                    ).build()
            )
            .features(ImmutableList.of(DQ_FLAG.getValue(), JUDGE_PILOT_FLAG.getValue()))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(directionsQuestionnaireService.getDirectionsCaseState(any()))
            .thenReturn(READY_FOR_JUDGE_DIRECTIONS.getValue());

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) mediationFailedCallbackHandler.handle(callbackParams);

        assertThat(response.getData()).containsEntry("state", READY_FOR_JUDGE_DIRECTIONS.getValue());
    }

    @Test
    public void shouldRaiseAppInsightWhenFeatureIsMediationPilot() {

        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList(ClaimFeatures.MEDIATION_PILOT.getValue()))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        handleSubmittedCallback();

        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.MEDIATION_PILOT_FAILED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void shouldRaiseAppInsightWhenFeatureIsNotMediationPilot() {

        Claim claim = claimSetForMediation.toBuilder()
            .features(Collections.singletonList(DQ_FLAG.getValue()))
            .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        handleSubmittedCallback();

        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.NON_MEDIATION_PILOT_FAILED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void shouldTriggerDQDeadlineEventIfOfflineDQ() {
        callbackParams = CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .build();

        Claim claim = claimSetForMediation.toBuilder()
                .claimData(SampleClaimData.submittedWithAmountMoreThanThousand())
                .build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        mediationFailedCallbackHandler.handle(callbackParams);

        verify(coreCaseDataService)
                .saveCaseEvent(AUTHORISATION, claim.getCcdCaseId(), CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE);
    }

    @Test
    public void testReadyForDirectionsDateForMediationUnsuccessful() {
        var dateTime = LocalDateTime.parse(INSTANT_EXPECTED);

        Claim claim = claimSetForMediation.toBuilder()
            .response(
                SampleResponse.FullDefence.builder().build()).build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseMapper.to(any(Claim.class))).thenReturn(getCcdCase(dateTime));
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(getCcdCaseMap(dateTime));

        var response = (AboutToStartOrSubmitCallbackResponse) mediationFailedCallbackHandler.handle(callbackParams);

        CCDCase ccdCase = (CCDCase) response.getData().get(DATE_REFERRED_FOR_DIRECTIONS);

        assertEquals(dateTime, ccdCase.getDateReferredForDirections());
    }

    private void handleSubmittedCallback() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.MEDIATION_FAILED.getValue())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        mediationFailedCallbackHandler.handle(callbackParams);
    }

    private CCDCase getCcdCase(LocalDateTime dateTime) {
        return CCDCase.builder()
            .dateReferredForDirections(dateTime)
            .build();
    }

    private Map<String, Object> getCcdCaseMap(LocalDateTime dateTime) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATE_REFERRED_FOR_DIRECTIONS, getCcdCase(dateTime));
        map.put(REFERENCE_KEY, REFERENCE);
        return map;
    }
}
