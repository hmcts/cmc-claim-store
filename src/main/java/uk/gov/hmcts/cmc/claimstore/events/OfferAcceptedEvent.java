package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;

public class OfferAcceptedEvent extends OfferDecidedEvent {

    public OfferAcceptedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }
}
