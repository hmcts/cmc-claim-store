package uk.gov.hmcts.cmc.claimstore.models.offers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settlement {

    private SettlementStatus status = SettlementStatus.unsettled;
    private List<PartyStatement> partyStatements = new ArrayList<>();

    public void makeOffer(Offer offer, MadeBy madeBy) {

    }

    public SettlementStatus getStatus() {
        return status;
    }

    public List<PartyStatement> getPartyStatements() {
        return Collections.unmodifiableList(partyStatements);
    }

}
