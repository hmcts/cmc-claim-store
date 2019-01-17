package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_REJECTED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementServiceTest {

    private static final String AUTHORISATION = "Bearer aaa";

    private SettlementAgreementService settlementAgreementService;

    @Mock
    private ClaimService claimService;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private AppInsights appInsights;

    @Mock
    private CCDEventProducer ccdEventProducer;

    @Before
    public void setup() {
        settlementAgreementService =
            new SettlementAgreementService(claimService, caseRepository,
                eventProducer, appInsights, ccdEventProducer);
    }

    @Test
    public void shouldSuccessfullyRejectSettlementAgreement() {
        Claim claimWithSettlementAgreement = buildClaimWithSettlementAgreementOffer();

        when(claimService.getClaimByExternalId(claimWithSettlementAgreement.getExternalId(), AUTHORISATION))
            .thenReturn(claimWithSettlementAgreement);

        settlementAgreementService.reject(claimWithSettlementAgreement, AUTHORISATION);

        verify(caseRepository).updateSettlement(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_REJECTED_BY_DEFENDANT.getValue()));

        verify(ccdEventProducer).createCCDSettlementEvent(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_REJECTED_BY_DEFENDANT.getValue()));
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnSettlementAgreementAlreadyRejected() {
        Claim claimWithSettlementAgreementRejected = buildClaimWithSettlementAgreementRejected();
        settlementAgreementService.reject(claimWithSettlementAgreementRejected, AUTHORISATION);
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnClaimWithNoSettlement() {
        Claim claim = SampleClaim.builder().build();
        settlementAgreementService.reject(claim, AUTHORISATION);
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnRejectWhenSettlementAgreementAlreadyReached() {
        Claim claim = buildClaimWithSettlementReached();
        settlementAgreementService.reject(claim, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyCountersignSettlementAgreement() {
        Claim claimWithSettlementAgreement = buildClaimWithSettlementAgreementOffer();

        when(claimService.getClaimByExternalId(claimWithSettlementAgreement.getExternalId(), AUTHORISATION))
            .thenReturn(claimWithSettlementAgreement);

        settlementAgreementService.countersign(claimWithSettlementAgreement, AUTHORISATION);

        InOrder inOrder = inOrder(caseRepository, eventProducer, ccdEventProducer, appInsights);

        inOrder.verify(caseRepository).updateSettlement(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT.getValue()));

        inOrder.verify(ccdEventProducer).createCCDSettlementEvent(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT.getValue()));

        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.SETTLEMENT_AGREEMENT_REACHED),
            eq(REFERENCE_NUMBER), eq(claimWithSettlementAgreement.getReferenceNumber()));
    }

    @Test
    public void shouldSuccessfullyCountersignSettlementAgreementByAdmission() {
        Claim claimWithSettlementAgreement = buildClaimWithSettlementPaymentIntention();

        when(claimService.getClaimByExternalId(claimWithSettlementAgreement.getExternalId(), AUTHORISATION))
            .thenReturn(claimWithSettlementAgreement);

        settlementAgreementService.countersign(claimWithSettlementAgreement, AUTHORISATION);

        InOrder inOrder = inOrder(caseRepository, eventProducer, ccdEventProducer, appInsights);

        inOrder.verify(caseRepository).updateSettlement(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT.getValue()));

        inOrder.verify(ccdEventProducer).createCCDSettlementEvent(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT.getValue()));

        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.SETTLEMENT_AGREEMENT_REACHED_BY_ADMISSION),
            eq(REFERENCE_NUMBER), eq(claimWithSettlementAgreement.getReferenceNumber()));

    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionWhenCountersigningAgreementAlreadyRejected() {
        Claim claim = buildClaimWithSettlementAgreementRejected();
        settlementAgreementService.countersign(claim, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullySignSettlementAgreement() {
        // given
        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(claim.getExternalId()),
            eq(AUTHORISATION))).thenReturn(claim);

        //when
        settlementAgreementService.signSettlementAgreement(claim.getExternalId(), buildSettlement(), AUTHORISATION);

        //then
        verify(caseRepository)
            .updateSettlement(eq(claim), any(Settlement.class), eq(AUTHORISATION), anyString());

        verify(eventProducer).createSignSettlementAgreementEvent(eq(claim));

        verify(ccdEventProducer)
            .createCCDSettlementEvent(eq(claim), any(Settlement.class), eq(AUTHORISATION), anyString());

    }

    @Test(expected = ConflictException.class)
    public void signSettlementAgreementShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        // given
        Claim settledClaim = SampleClaim.builder()
            .withSettlementReachedAt(LocalDateTime.now()).build();
        when(claimService.getClaimByExternalId(eq(settledClaim.getExternalId()),
            eq(AUTHORISATION))).thenReturn(settledClaim);

        //when
        settlementAgreementService.signSettlementAgreement(settledClaim.getExternalId(),
            buildSettlement(), AUTHORISATION);
    }

    private Claim buildClaimWithSettlementAgreementOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private Claim buildClaimWithSettlementAgreementRejected() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private Claim buildClaimWithSettlementReached() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);
        settlement.countersign(MadeBy.DEFENDANT);

        return SampleClaim.builder().withSettlement(settlement)
            .withSettlementReachedAt(LocalDateTime.now()).build();
    }

    private static Settlement buildSettlement() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(
            Offer.builder()
                .content("Defendant's admission content")
                .completionDate(LocalDate.now().plusDays(60))
                .build(),
            MadeBy.DEFENDANT);

        settlement.accept(MadeBy.CLAIMANT);

        return settlement;
    }

    private Claim buildClaimWithSettlementPaymentIntention() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builderWithPaymentIntention().build(), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);

        return SampleClaim.builder().withSettlement(settlement).build();
    }
}
