package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.offers.Settlement;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferServiceTest {

    private static final String settlementJsonMock = "I'm expected to be settlement json";
    private static final Offer offer = mock(Offer.class);
    private static final MadeBy madeBy = MadeBy.DEFENDANT;
    private static final MadeBy decidedBy = MadeBy.CLAIMANT;
    private static final Claim claim = SampleClaim.getDefault();
    private static final Claim claimWithOffer = buildClaimWithOffer();
    private static final Claim claimWithAcceptedOffer = SampleClaim.builder()
        .withSettlementReachedAt(LocalDateTime.now()).build();

    private OffersService offersService;

    @Mock
    private OffersRepository offersRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private JsonMapper jsonMapper;

    @Before
    public void setup() {
        offersService = new OffersService(offersRepository, eventProducer, jsonMapper);
        when(jsonMapper.toJson(any(Settlement.class))).thenReturn(settlementJsonMock);
    }

    @Test
    public void shouldSuccessfullySavedOffer() {
        // when
        offersService.makeOffer(claim, offer, madeBy);

        //then
        verify(offersRepository).updateSettlement(eq(claim.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferMadeEvent(eq(claim));
    }

    @Test(expected = ConflictException.class)
    public void makeAnOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.makeOffer(claimWithAcceptedOffer, offer, madeBy);
    }

    @Test
    public void shouldSuccessfullyAcceptOffer() {
        // given
        Claim claimWithOffer = buildClaimWithOffer();

        // when
        offersService.accept(claimWithOffer, decidedBy);

        //then
        verify(offersRepository)
            .acceptOffer(eq(claimWithOffer.getId()), eq(settlementJsonMock), any(LocalDateTime.class));
        verify(eventProducer).createOfferAcceptedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void acceptOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.accept(claimWithAcceptedOffer, decidedBy);
    }

    @Test
    public void shouldSuccessfullyRejectOffer() {
        // when
        offersService.reject(claimWithOffer, decidedBy);

        //then
        verify(offersRepository).updateSettlement(eq(claimWithOffer.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferRejectedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test(expected = ConflictException.class)
    public void rejectOfferShouldThrowConflictExceptionWhenSettlementAlreadyReached() {
        offersService.reject(claimWithAcceptedOffer, decidedBy);
    }

    private static Claim buildClaimWithOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), madeBy);

        return SampleClaim.builder().withSettlement(settlement).build();
    }
}
