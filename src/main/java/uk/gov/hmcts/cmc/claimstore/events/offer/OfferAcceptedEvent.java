package uk.gov.hmcts.cmc.claimstore.events.offer;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class OfferAcceptedEvent extends OfferRespondedEvent {

    public OfferAcceptedEvent(Claim claim, MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
