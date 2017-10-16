package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;

@Service
public class OffersService {

    public Claim makeOffer(Claim claim, Offer offer, MadeBy party) {
        throw new RuntimeException("Not implemented yet!");
    }

}
