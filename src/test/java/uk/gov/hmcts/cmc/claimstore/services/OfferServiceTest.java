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
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferServiceTest {

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
    }

    @Test
    public void shouldSuccessfullySavedOffer() {

        // given
        String settlementJsonMock = "I'm expected to be partyStatement json";
        when(jsonMapper.toJson(any(Settlement.class))).thenReturn(settlementJsonMock);
        Claim claim = SampleClaim.getDefault();
        Offer offer = mock(Offer.class);
        MadeBy madeBy = MadeBy.DEFENDANT;

        // when
        offersService.makeOffer(claim, offer, madeBy);

        //then
        verify(offersRepository).updateSettlement(eq(claim.getId()), eq(settlementJsonMock));
        verify(eventProducer).createOfferMadeEvent(eq(claim));
    }
}
