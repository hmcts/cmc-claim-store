package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantResponseRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
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
    private AppInsights appInsights;

    @Mock
    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;

    @Before
    public void setUp() {
        claimantResponseService = new ClaimantResponseService(
            claimService,
            appInsights,
            caseRepository,
            new ClaimantResponseRule(),
            eventProducer,
            formaliseResponseAcceptanceService
        );
    }

    @Test
    public void saveResponseRejection() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .build();

        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseRejection.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_REJECTED), eq(claim.getReferenceNumber()));
        verify(formaliseResponseAcceptanceService, times(0))
            .formalise(any(), any(), anyString());
    }

    @Test
    public void saveResponseAcceptation() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .build();

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, once())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED), eq(claim.getReferenceNumber()));

    }

    @Test
    public void saveResponseAcceptationShouldSucceedWhenStatesPaidWithNoFormalisation() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build())
            .withRespondedAt(LocalDateTime.now().minusDays(32))
            .build();

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveClaimantResponse(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION)))
            .thenReturn(claim);

        InOrder inOrder = inOrder(caseRepository, formaliseResponseAcceptanceService, eventProducer, appInsights);

        claimantResponseService.save(EXTERNAL_ID, claim.getSubmitterId(), claimantResponse, AUTHORISATION);

        inOrder.verify(caseRepository, once()).saveClaimantResponse(any(Claim.class), eq(claimantResponse), any());
        inOrder.verify(formaliseResponseAcceptanceService, never())
            .formalise(any(Claim.class), any(ResponseAcceptation.class), eq(AUTHORISATION));
        inOrder.verify(eventProducer, once()).createClaimantResponseEvent(any(Claim.class));
        inOrder.verify(appInsights, once()).trackEvent(eq(CLAIMANT_RESPONSE_ACCEPTED), eq(claim.getReferenceNumber()));
    }
}
