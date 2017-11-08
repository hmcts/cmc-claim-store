package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;

public class OfferRejectedEvent extends OfferDecidedEvent {

    public OfferRejectedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
