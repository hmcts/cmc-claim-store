package uk.gov.hmcts.cmc.claimstore.models.offers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.cmc.claimstore.exceptions.IllegalSettlementStatementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settlement {

    private List<PartyStatement> partyStatements = new ArrayList<>();

    public void makeOffer(Offer offer, MadeBy party) {
        assertOfferCanBeMadeBy(party);
        partyStatements.add(new PartyStatement(StatementType.OFFER, party, offer));
    }

    public void accept(MadeBy party) {
        assertOfferCanBeAccepted(party);
        partyStatements.add(new PartyStatement(StatementType.ACCEPTATION, party));
    }

    @JsonIgnore
    PartyStatement getLastStatement() {
        if (partyStatements.isEmpty()) {
            throw new IllegalStateException("No statements have yet been made during that settlement");
        }
        return partyStatements.get(partyStatements.size() - 1);
    }

    public List<PartyStatement> getPartyStatements() {
        return Collections.unmodifiableList(partyStatements);
    }

    private void assertOfferCanBeMadeBy(MadeBy party) {
        if (!partyStatements.isEmpty() && lastStatementIsAnOfferMadeBy(party)) {
            throw new IllegalSettlementStatementException("You cannot make multiple offers in a row");
        }
    }

    @JsonIgnore
    private void assertOfferCanBeAccepted(MadeBy party) {
        assertOfferCanBeMadeBy(party);

        if (partyStatements.isEmpty()) {
            throw new IllegalSettlementStatementException("Offer has not been made. You can't accept it!");
        }

        if (!lastStatementIsOffer()) {
            throw new IllegalSettlementStatementException(
                "Offer was already " + getLastStatement().getType().toString().toLowerCase()
            );
        }
    }

    private boolean lastStatementIsAnOfferMadeBy(MadeBy madeBy) {
        return lastStatementIsOffer() && getLastStatement().getMadeBy().equals(madeBy);
    }

    private boolean lastStatementIsOffer() {
        return getLastStatement().getType().equals(StatementType.OFFER);
    }
}
