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

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final Offer offer = mock(Offer.class);
    private static final MadeBy madeBy = MadeBy.DEFENDANT;
    private static final MadeBy decidedBy = MadeBy.CLAIMANT;
    private static final Claim claim = SampleClaim.getDefault();
    private static final Claim claimWithOffer = buildClaimWithOffer();
    private static final Claim claimWithAcceptedOffer = SampleClaim.builder()
        .withSettlementReachedAt(LocalDateTime.now()).build();

    private OffersService offersService;

    @Mock
    private ClaimService claimService;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private TimeService timeService;

    @Before
    public void setup() {
        offersService = new OffersService(claimService, caseRepository, eventProducer, timeService);
    }

    @Test
    public void shouldSuccessfullySavedOffer() {
        // when
        offersService.makeOffer(claim, offer, madeBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claim), any(Settlement.class),
            eq(AUTHORISATION), eq(OFFER_MADE_BY_DEFENDANT), eq(null));

        verify(eventProducer).createOfferMadeEvent(eq(claim));
    }

    @Test(expected = ConflictException.class)
    public void makeAnOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.makeOffer(claimWithAcceptedOffer, offer, madeBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyAcceptOffer() {
        // given
        Claim claimWithOffer = buildClaimWithOffer();
        Claim acceptedOffer = buildClaimWithAcceptedOffer();
        LocalDateTime now = LocalDateTime.now();
        when(claimService.getClaimById(eq(claimWithOffer.getId()))).thenReturn(acceptedOffer);
        when(timeService.nowInLocalZone()).thenReturn(now);
        Settlement settlement = acceptedOffer.getSettlement().orElse(null);
        // when
        offersService.accept(claimWithOffer, decidedBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claimWithOffer), eq(settlement),
            eq(AUTHORISATION), eq(OFFER_ACCEPTED_BY_CLAIMANT), eq(now));

        verify(eventProducer).createOfferAcceptedEvent(eq(acceptedOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void acceptOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.accept(claimWithAcceptedOffer, decidedBy, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyRejectOffer() {
        // when
        Claim claimWithOffer = OfferServiceTest.claimWithOffer;
        offersService.reject(claimWithOffer, decidedBy, AUTHORISATION);

        //then
        verify(caseRepository).updateSettlement(eq(claimWithOffer), any(Settlement.class),
            eq(AUTHORISATION), eq(OFFER_REJECTED_BY_CLAIMANT), eq(null));

        verify(eventProducer).createOfferRejectedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void rejectOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.reject(claimWithAcceptedOffer, decidedBy, AUTHORISATION);
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
