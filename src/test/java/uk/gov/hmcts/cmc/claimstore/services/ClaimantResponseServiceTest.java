package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGNING_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LIFT_STAY;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.STAY_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_RESPONSE_ACCEPTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_STAYED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.LA_PILOT_ELIGIBLE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_NON_PILOT_ELIGIBLE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_PILOT_ELIGIBLE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NON_LA_CASES;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private ClaimantResponseService claimantResponseService;

    @Mock
    private ClaimService claimService;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private AppInsights appInsights;

    @Mock
    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;

    @Mock
    private DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;

    @Before
    public void setUp() {
        claimantResponseService = new ClaimantResponseService(
            claimService,
            appInsights,
            caseRepository,
            new ClaimantResponseRule(),
            eventProducer,
            formaliseResponseAcceptanceService,
            directionsQuestionnaireDeadlineCalculator
        );
    }

    @Test
    public void saveResponseRejectionWithoutDirectionsQuestionnaire() {
        ReflectionTestUtils.setField(claimantResponseService, "directionsQuestionnaireEnabled", true);
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class), anyString());
        inOrder.verify(appInsights, once()).trackEvent(eq(NON_LA_CASES),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
        verify(formaliseResponseAcceptanceService, times(0))
            .formalise(any(), any(), anyString());
        verify(caseRepository, never()).saveCaseEvent(AUTHORISATION, claim, ASSIGNING_FOR_DIRECTIONS);
        verify(caseRepository, never()).saveCaseEvent(AUTHORISATION, claim, REFERRED_TO_MEDIATION);
    }

    @Test
    public void saveCaseEventCaseStayedAfterFullDefenseDispute() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE).build())
            .withRespondedAt(LocalDateTime.now())
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(appInsights).trackEvent(CLAIM_STAYED, REFERENCE_NUMBER, claim.getReferenceNumber());
        verify(caseRepository).saveCaseEvent(AUTHORISATION, claim, STAY_CLAIM);

    }

    @Test
    public void saveResponseRejectionWithDirectionsQuestionnaireButDQDisabled() {
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(DirectionsQuestionnaire.builder()
                .build())
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository, never()).saveCaseEvent(AUTHORISATION, claim, ASSIGNING_FOR_DIRECTIONS);
        verify(caseRepository, never()).saveCaseEvent(AUTHORISATION, claim, REFERRED_TO_MEDIATION);
    }

    @Test
    public void saveResponseRejectionWithDirectionsQuestionnaire() {
        ReflectionTestUtils.setField(claimantResponseService, "directionsQuestionnaireEnabled", true);

        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .freeMediation(YES)
            .directionsQuestionnaire(DirectionsQuestionnaire.builder()
                .build())
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository).saveCaseEvent(AUTHORISATION, claim, REFERRED_TO_MEDIATION);
    }

    @Test
    public void saveResponseAcceptation() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class), anyString());
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));

    }

    @Test
    public void saveResponseAcceptationReferredToJudge() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationReferToJudgeWithCourtDetermination();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, appInsights);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(appInsights, once()).trackEvent(CLAIMANT_RESPONSE_ACCEPTED,
            REFERENCE_NUMBER,
            claim.getReferenceNumber());

        verify(eventProducer).createClaimantResponseEvent(any(Claim.class), anyString());
    }

    @Test
    public void saveResponseAcceptationReferredToJudgeWithDefendantAsCompany() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(null)
            .withAmountPaid(new BigDecimal(100))
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        InOrder inOrder = inOrder(
            caseRepository,
            formaliseResponseAcceptanceService,
            eventProducer,
            appInsights);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, never())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class), anyString());
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionPartAdmitWithNoMediationShouldUpdateDirectionsQuestionnaireDeadlineIfNoOnlineDQ() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);
        final LocalDate dqDeadline = respondedAt.plusDays(19).toLocalDate();

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        final Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().build())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(directionsQuestionnaireDeadlineCalculator
            .calculateDirectionsQuestionnaireDeadlineCalculator(any()))
            .thenReturn(dqDeadline);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        verify(directionsQuestionnaireDeadlineCalculator)
            .calculateDirectionsQuestionnaireDeadlineCalculator(any(LocalDateTime.class));
        verify(caseRepository).updateDirectionsQuestionnaireDeadline(any(Claim.class), eq(dqDeadline), anyString());
        verify(eventProducer).createClaimantResponseEvent(any(Claim.class), anyString());
        verify(appInsights).trackEvent(eq(NON_LA_CASES),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionPartAdmitNoMediationShouldNotUpdateDirectionsQuestionnaireDeadlineIfOnlineDQ() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        final Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of("directionsQuestionnaire"))
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().build())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        verify(directionsQuestionnaireDeadlineCalculator, never())
            .calculateDirectionsQuestionnaireDeadlineCalculator(any());
        verify(caseRepository, never()).updateDirectionsQuestionnaireDeadline(
            any(Claim.class),
            any(LocalDate.class),
            anyString());
        verify(eventProducer).createClaimantResponseEvent(any(Claim.class), eq(AUTHORISATION));
        verify(appInsights).trackEvent(eq(LA_PILOT_ELIGIBLE),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionFullDefenceWithNoMediationShouldUpdateDirectionsQuestionnaireDeadlineIfNoOnlineDQ() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);
        final LocalDate dqDeadline = respondedAt.plusDays(19).toLocalDate();

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        final Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullDefence.builder().build())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(directionsQuestionnaireDeadlineCalculator
            .calculateDirectionsQuestionnaireDeadlineCalculator(any()))
            .thenReturn(dqDeadline);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        verify(directionsQuestionnaireDeadlineCalculator)
            .calculateDirectionsQuestionnaireDeadlineCalculator(any(LocalDateTime.class));
        verify(caseRepository).updateDirectionsQuestionnaireDeadline(any(Claim.class), eq(dqDeadline), anyString());
        verify(eventProducer).createClaimantResponseEvent(any(Claim.class), eq(AUTHORISATION));
        verify(appInsights).trackEvent(eq(NON_LA_CASES),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionFullDefenceNoMediationShouldNotUpdateDirectionsQuestionnaireDeadlineIfOnlineDQ() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        final Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of("directionsQuestionnaire"))
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullDefence.builder().build())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(caseRepository).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        verify(directionsQuestionnaireDeadlineCalculator, never())
            .calculateDirectionsQuestionnaireDeadlineCalculator(any());
        verify(caseRepository, never()).updateDirectionsQuestionnaireDeadline(
            any(Claim.class),
            any(LocalDate.class),
            anyString());
        verify(eventProducer).createClaimantResponseEvent(any(Claim.class), eq(AUTHORISATION));
        verify(appInsights).trackEvent(eq(LA_PILOT_ELIGIBLE),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionPartAdmissionWithMediationShouldSendMediationPilotEligibleInsight() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validRejectionWithFreeMediation();

        final Claim claim = SampleClaim.builder()
            .withFeatures(ImmutableList.of("mediationPilot"))
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithFreeMediation())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(appInsights).trackEvent(eq(MEDIATION_PILOT_ELIGIBLE),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void rejectionPartAdmissionWithMediationShouldSendMediationNonPilotEligibleInsight() {
        final LocalDateTime respondedAt = LocalDateTime.now().minusDays(10);

        final ClaimantResponse claimantResponse = SampleClaimantResponse.validRejectionWithFreeMediation();

        final Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithFreeMediation())
            .withRespondedAt(respondedAt)
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        verify(appInsights).trackEvent(eq(MEDIATION_NON_PILOT_ELIGIBLE),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveResponseAcceptationShouldSucceedWhenPartAdmitPayImmediatelyWithNoFormalisation() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(null)
            .withAmountPaid(new BigDecimal(100))
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(
                SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately()
            )
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, never())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class), anyString());
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void shouldNotRaiseGenericClaimantResponseEventOnSettlementAgreement() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(FormaliseOption.SETTLEMENT)
            .withAmountPaid(new BigDecimal(100))
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(
                SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate()
            )
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));

        verify(eventProducer, never()).createClaimantResponseEvent(any(Claim.class), anyString());
    }

    @Test
    public void shouldLiftStayedClaimWhenPartAdmission() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(FormaliseOption.SETTLEMENT)
            .withAmountPaid(new BigDecimal(100))
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(
                SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate()
            )
            .withRespondedAt(LocalDateTime.now().minusDays(34))
            .withClaimantResponse(claimantResponse)
            .withState(ClaimState.STAYED)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);
        when(caseRepository.saveCaseEvent(eq(AUTHORISATION), eq(claim), eq(LIFT_STAY))).thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);
        verify(caseRepository, once()).saveCaseEvent(AUTHORISATION, claim, LIFT_STAY);
    }

    @Test
    public void shouldNotLiftStayedClaimWhenFullDefence() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(FormaliseOption.SETTLEMENT)
            .withAmountPaid(new BigDecimal(100))
            .build();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build())
            .withRespondedAt(LocalDateTime.now().minusDays(34))
            .withClaimantResponse(claimantResponse)
            .withState(ClaimState.STAYED)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);
        verify(caseRepository, never()).saveCaseEvent(AUTHORISATION, claim, LIFT_STAY);
    }
}
