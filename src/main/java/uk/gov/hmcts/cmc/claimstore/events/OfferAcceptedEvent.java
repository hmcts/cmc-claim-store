package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.offers.MadeBy;

public class OfferAcceptedEvent extends OfferRespondedEvent {

    public OfferAcceptedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
