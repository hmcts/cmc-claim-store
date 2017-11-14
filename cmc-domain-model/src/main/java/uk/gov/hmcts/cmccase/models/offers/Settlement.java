package uk.gov.hmcts.cmccase.models.offers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.cmccase.exceptions.IllegalSettlementStatementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class Settlement {

    private List<PartyStatement> partyStatements = new ArrayList<>();

    public void makeOffer(Offer offer, MadeBy party) {
        assertOfferCanBeMadeBy(party);
        partyStatements.add(new PartyStatement(StatementType.OFFER, party, offer));
    }

    public void accept(MadeBy party) {
        assertOfferCanBeResponded(party);
        partyStatements.add(new PartyStatement(StatementType.ACCEPTATION, party));
    }

    public void reject(MadeBy party) {
        assertOfferCanBeResponded(party);
        partyStatements.add(new PartyStatement(StatementType.REJECTION, party));
    }

    @JsonIgnore
    PartyStatement getLastStatement() {
        if (partyStatements.isEmpty()) {
            throw new IllegalSettlementStatementException("No statements have yet been made during that settlement");
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

    private void assertOfferCanBeResponded(MadeBy party) {
        assertOfferCanBeMadeBy(party);

        if (!lastStatementIsOffer()) {
            throw new IllegalSettlementStatementException(
                format("Last statement was: %s , offer expected.", getLastStatement().getType().name().toLowerCase())
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
