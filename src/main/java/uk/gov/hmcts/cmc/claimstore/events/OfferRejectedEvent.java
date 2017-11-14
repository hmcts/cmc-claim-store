package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.offers.MadeBy;

public class OfferRejectedEvent extends OfferRespondedEvent {

    public OfferRejectedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
