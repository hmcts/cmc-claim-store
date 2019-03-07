package uk.gov.hmcts.cmc.claimstore.events.settlement;

import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRespondedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class CountersignSettlementAgreementEvent extends OfferRespondedEvent {
    private final String authorisation;

    public CountersignSettlementAgreementEvent(Claim claim, String authorisation) {
        this.claim = claim;
        this.party = MadeBy.DEFENDANT;
        this.authorisation = authorisation;
    }

    public String getAuthorisation() {
        return this.authorisation;
    }
}
