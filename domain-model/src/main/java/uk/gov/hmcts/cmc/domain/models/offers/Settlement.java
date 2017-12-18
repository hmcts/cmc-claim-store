package uk.gov.hmcts.cmc.domain.models.offers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Settlement {

    private static final String NO_STATEMENTS_MADE = "No statements have yet been made during that settlement";
    private final List<PartyStatement> partyStatements = new ArrayList<>();

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
            throw new IllegalSettlementStatementException(NO_STATEMENTS_MADE);
        }
        return partyStatements.get(partyStatements.size() - 1);
    }

    @JsonIgnore
    public PartyStatement getLastOfferStatement() {
        if (partyStatements.isEmpty()) {
            throw new IllegalSettlementStatementException(NO_STATEMENTS_MADE);
        }

        List<PartyStatement> tmpList = new ArrayList<>(partyStatements);
        Collections.reverse(tmpList);

        return tmpList.stream()
            .filter((partyStatement -> partyStatement.getType() == StatementType.OFFER))
            .findFirst()
            .orElseThrow(() -> new IllegalSettlementStatementException("No statements with an offer found"));

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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
