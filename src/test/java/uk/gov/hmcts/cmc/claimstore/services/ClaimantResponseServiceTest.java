package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
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
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_RESPONSE_ACCEPTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIMANT_RESPONSE_REJECTED;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
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
    private CCDEventProducer ccdEventProducer;

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
            directionsQuestionnaireDeadlineCalculator,
            ccdEventProducer);
    }

    @Test
    public void saveResponseRejection() {

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
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_REJECTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
        verify(formaliseResponseAcceptanceService, times(0))
            .formalise(any(), any(), anyString());
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
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
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
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstallments())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, ccdEventProducer, appInsights);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(ccdEventProducer)
            .createCCDClaimantResponseEvent(any(Claim.class), eq(claimantResponse), eq(AUTHORISATION));
        inOrder.verify(appInsights, once()).trackEvent(CLAIMANT_RESPONSE_ACCEPTED,
            REFERENCE_NUMBER,
            claim.getReferenceNumber());

        verify(eventProducer, never()).createClaimantResponseEvent(any(Claim.class));
    }

    @Test
    public void saveResponseAcceptationReferredToJudgeWithDefendantAsCompany() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationReferToJudgeWithCourtDetermination();

        Party company = SampleParty.builder().company();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstallmentsAndParty(company))
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
            ccdEventProducer,
            appInsights);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(ccdEventProducer)
            .createCCDClaimantResponseEvent(any(Claim.class), eq(claimantResponse), eq(AUTHORISATION));
        inOrder.verify(appInsights, once()).trackEvent(CLAIMANT_RESPONSE_ACCEPTED,
            REFERENCE_NUMBER,
            claim.getReferenceNumber());
    }

    @Test
    public void saveResponseAcceptationReferredToJudgeWithDefendantAsOrganisation() {

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationReferToJudgeWithCourtDetermination();

        Party organisation = SampleParty.builder().organisation();

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse
                .PartAdmission
                .builder()
                .buildWithPaymentOptionInstallmentsAndParty(organisation))
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .withClaimantResponse(claimantResponse)
            .build();


        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        InOrder inOrder = inOrder(caseRepository,
            formaliseResponseAcceptanceService,
            eventProducer,
            ccdEventProducer,
            appInsights);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(ccdEventProducer)
            .createCCDClaimantResponseEvent(any(Claim.class), eq(claimantResponse), eq(AUTHORISATION));
        inOrder.verify(appInsights, once()).trackEvent(CLAIMANT_RESPONSE_ACCEPTED,
            REFERENCE_NUMBER,
            claim.getReferenceNumber());
    }

    @Test
    public void saveResponseAcceptationShouldSucceedWhenStatesPaidWithNoFormalisation() {

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

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, never())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveResponseRejectionOfPartAdmitWithNoMediationShouldUpdateDirectionsQuestionnaireDeadline() {
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
        verify(eventProducer).createClaimantResponseEvent(any(Claim.class));
        verify(appInsights).trackEvent(eq(CLAIMANT_RESPONSE_REJECTED),
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
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }
}
