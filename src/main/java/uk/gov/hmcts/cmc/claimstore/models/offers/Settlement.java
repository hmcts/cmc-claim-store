package uk.gov.hmcts.cmc.claimstore.models.offers;

import java.util.List;

public class Settlement {

    private SettlementStatus status = SettlementStatus.UNSETTLED;
    private List<PartyStatement> partyStatements;

    public SettlementStatus getStatus() {
        return status;
    }

}
