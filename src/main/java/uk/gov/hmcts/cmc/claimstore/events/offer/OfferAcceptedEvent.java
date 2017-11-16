package uk.gov.hmcts.cmc.claimstore.events.offer;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class OfferAcceptedEvent extends OfferRespondedEvent {

    public OfferAcceptedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
