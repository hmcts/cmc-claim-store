package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_MADE_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_REJECTED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_SIGNED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_SIGNED_BY_DEFENDANT;

@RunWith(MockitoJUnitRunner.class)
public class OfferServiceTest {

    private static final String AUTHORISATION = "Bearer aaa";
    private static final Offer offer = mock(Offer.class);
    private static final MadeBy madeBy = MadeBy.DEFENDANT;
    private static final MadeBy decidedBy = MadeBy.CLAIMANT;
    private static final Claim claim = SampleClaim.getDefault();
    private static final Claim settledClaim = SampleClaim.builder()
        .withSettlementReachedAt(LocalDateTime.now()).build();

    private final Claim claimWithOffer = buildClaimWithOffer();
    private final Claim claimWithAcceptedOffer = buildClaimWithAcceptedOffer();

    private OffersService offersService;

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

    @Before
    public void setup() {
        offersService = new OffersService(claimService, caseRepository, eventProducer, appInsights, ccdEventProducer);
    }

    @Test
    public void shouldSuccessfullySavedOffer() {
        //given
        when(claimService.getClaimByExternalId(eq(claimWithOffer.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claim);

        // when
        offersService.makeOffer(claim, offer, madeBy, AUTHORISATION);
        //then
        verify(caseRepository).updateSettlement(eq(claim), any(Settlement.class),
            eq(AUTHORISATION), eq(OFFER_MADE_BY_DEFENDANT.name()));

        verify(eventProducer).createOfferMadeEvent(eq(claim));
    }

    @Test(expected = ConflictException.class)
    public void makeAnOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.makeOffer(settledClaim, offer, madeBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyAcceptOffer() {
        // given
        Claim claimWithOffer = buildClaimWithOffer();
        Claim acceptedOffer = buildClaimWithAcceptedOffer();

        when(claimService.getClaimByExternalId(eq(claimWithOffer.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(acceptedOffer);

        Settlement settlement = acceptedOffer.getSettlement().orElse(null);
        // when
        offersService.accept(claimWithOffer, decidedBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claimWithOffer), eq(settlement),
            eq(AUTHORISATION), eq(OFFER_SIGNED_BY_CLAIMANT.name()));

        verify(eventProducer).createOfferAcceptedEvent(eq(acceptedOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void acceptOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.accept(settledClaim, decidedBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyRejectOffer() {
        //given
        when(claimService.getClaimByExternalId(eq(claimWithOffer.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(claimWithOffer);

        // when
        offersService.reject(claimWithOffer, decidedBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claimWithOffer), any(Settlement.class),
            eq(AUTHORISATION), eq(OFFER_REJECTED_BY_CLAIMANT.name()));

        verify(eventProducer).createOfferRejectedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void rejectOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.reject(settledClaim, decidedBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyCountersignAgreement() {
        // given
        when(claimService.getClaimByExternalId(eq(claimWithAcceptedOffer.getExternalId()),
            eq(AUTHORISATION))).thenReturn(settledClaim);

        // when
        offersService.countersign(claimWithAcceptedOffer, madeBy, AUTHORISATION);

        //then
        verify(caseRepository)
            .reachSettlementAgreement(eq(claimWithAcceptedOffer), any(Settlement.class), eq(AUTHORISATION),
                eq(OFFER_SIGNED_BY_DEFENDANT.name()));

        verify(eventProducer).createAgreementCountersignedEvent(eq(settledClaim), eq(madeBy));
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

    private static Claim buildClaimWithOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), madeBy);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private static Claim buildClaimWithAcceptedOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), madeBy);
        settlement.accept(MadeBy.CLAIMANT);

        return SampleClaim.builder()
            .withSettlement(settlement).build();
    }
}
