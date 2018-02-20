package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.services.search.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_ACCEPTED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_MADE_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_REJECTED_BY_CLAIMANT;

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

    @Before
    public void setup() {
        offersService = new OffersService(claimService, caseRepository, eventProducer);
    }

    @Test
    public void shouldSuccessfullySavedOffer() {
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
        // when
        offersService.accept(claimWithOffer, decidedBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claimWithOffer), any(Settlement.class),
            eq(AUTHORISATION), eq(OFFER_ACCEPTED_BY_CLAIMANT.name()));

        verify(eventProducer).createOfferAcceptedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void acceptOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.accept(settledClaim, decidedBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyRejectOffer() {
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
        when(claimService.getClaimById(eq(claimWithAcceptedOffer.getId()))).thenReturn(settledClaim);

        // when
        offersService.countersign(claimWithAcceptedOffer, madeBy, AUTHORISATION);

        //then
        verify(caseRepository)
            .reachSettlementAgreement(eq(claimWithAcceptedOffer), any(Settlement.class), eq(AUTHORISATION),
                eq("OFFER_COUNTERSIGNED_BY_DEFENDANT"));

        verify(eventProducer).createAgreementCountersignedEvent(eq(settledClaim), eq(madeBy));
    }

    private static Claim buildClaimWithOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), madeBy);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private static Claim buildClaimWithAcceptedOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), madeBy);
        settlement.accept(MadeBy.CLAIMANT);


        return SampleClaim.builder()
            .withSettlement(settlement).build();
    }
}
