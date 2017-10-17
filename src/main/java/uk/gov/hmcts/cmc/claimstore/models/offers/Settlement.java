package uk.gov.hmcts.cmc.claimstore.models.offers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settlement {

    private List<PartyStatement> partyStatements = new ArrayList<>();

    public void makeOffer(Offer offer, MadeBy madeBy) {
        if (!partyStatements.isEmpty() && getLastStatement().getType().equals(StatementType.offer) && getLastStatement().getMadeBy().equals(madeBy)) {
            throw new IllegalStateException("You cannot make multiple offers in a row");
        }
        partyStatements.add(new PartyStatement(StatementType.offer, madeBy, offer));
    }

    public PartyStatement getLastStatement() {
        return partyStatements.get(partyStatements.size() - 1);
    }

    public List<PartyStatement> getPartyStatements() {
        return Collections.unmodifiableList(partyStatements);
    }

}
