package uk.gov.hmcts.cmc.claimstore.events.offer;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

public class SettlementAgreementCountersignedEvent extends OfferRespondedEvent {
    public SettlementAgreementCountersignedEvent(Claim claim) {
        this.claim = claim;
        this.party = MadeBy.DEFENDANT;
    }
}
