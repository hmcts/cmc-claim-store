package uk.gov.hmcts.cmc.claimstore.events.offer;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class AgreementCountersignedEvent extends OfferRespondedEvent {
    private final String authorisation;

    public AgreementCountersignedEvent(Claim claim, MadeBy party, String authorisation) {
        this.claim = claim;
        this.party = party;
        this.authorisation = authorisation;
    }

    public String getAuthorisation() {
        return this.authorisation;
    }
}
