package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.offers.Settlement;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;

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
        // given
        Claim claim = SampleClaim.getDefault();

        // when
        offersService.makeOffer(claim, offer, madeBy);

        //then
        verify(offersRepository).updateSettlement(eq(claim.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferMadeEvent(eq(claim));
    }

    @Test
    public void shouldSuccessfullyAcceptOffer() {
        // given
        Claim claimWithOffer = buildClaimWithOffer();

        // when
        offersService.accept(claimWithOffer, decidedBy);

        //then
        verify(offersRepository).updateSettlement(eq(claimWithOffer.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferAcceptedEvent(eq(claimWithOffer), eq(decidedBy));
    }

    @Test
    public void shouldSuccessfullyRejectOffer() {
        // given
        Claim claim = buildClaimWithOffer();

        // when
        offersService.reject(claim, decidedBy);

        //then
        verify(offersRepository).updateSettlement(eq(claim.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferRejectedEvent(eq(claim), eq(decidedBy));
    }

    private Claim buildClaimWithOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), madeBy);

        return SampleClaim.builder().withSettlement(settlement).build();
    }
}
