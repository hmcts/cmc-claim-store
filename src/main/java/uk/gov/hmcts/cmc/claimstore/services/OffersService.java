package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    public OffersService(
        OffersRepository offersRepository,
        JsonMapper jsonMapper
    ) {
        this.offersRepository = offersRepository;
        this.jsonMapper = jsonMapper;
    }

    public void makeOffer(Claim claim, Offer offer, MadeBy party) {
        Settlement settlement = claim.getSettlement().orElse(new Settlement());
        settlement.makeOffer(offer, party);
        offersRepository.updateSettlement(claim.getId(), jsonMapper.toJson(settlement));
    }

}
