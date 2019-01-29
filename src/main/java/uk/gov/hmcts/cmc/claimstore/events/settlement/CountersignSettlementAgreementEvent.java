package uk.gov.hmcts.cmc.claimstore.events.settlement;

import uk.gov.hmcts.cmc.claimstore.events.offer.OfferRespondedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class CountersignSettlementAgreementEvent extends OfferRespondedEvent {
    public CountersignSettlementAgreementEvent(Claim claim) {
        this.claim = claim;
        this.party = MadeBy.DEFENDANT;
    }
}
