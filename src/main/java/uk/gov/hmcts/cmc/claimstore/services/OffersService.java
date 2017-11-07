package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.offers.Settlement;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.OffersRepository;

@Service
public class OffersService {

    private final OffersRepository offersRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;

    @Autowired
    public OffersService(
        OffersRepository offersRepository,
        EventProducer eventProducer,
        JsonMapper jsonMapper
    ) {
        this.offersRepository = offersRepository;
        this.eventProducer = eventProducer;
        this.jsonMapper = jsonMapper;
    }

    public void makeOffer(Claim claim, Offer offer, MadeBy party) {
        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party);
        offersRepository.updateSettlement(claim.getId(), jsonMapper.toJson(settlement));
        eventProducer.createOfferMadeEvent(claim);
    }

    public void accept(Claim claim, MadeBy party) {
        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.accept(party);
        offersRepository.updateSettlement(claim.getId(), jsonMapper.toJson(settlement));
        eventProducer.createOfferAcceptedEvent(claim, party);
    }
}
