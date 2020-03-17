package uk.gov.hmcts.cmc.domain.models.offers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class Settlement {

    private static final String NO_STATEMENTS_MADE = "No statements have yet been made during that settlement";
    private final List<PartyStatement> partyStatements = new ArrayList<>();

    public void makeOffer(Offer offer, MadeBy party, String partyStatementId) {
        assertOfferCanBeMadeBy(party);
        addOffer(offer, party, partyStatementId);
    }

    public void addOffer(Offer offer, MadeBy party, String partyStatementId) {
        partyStatements.add(PartyStatement.builder().type(StatementType.OFFER)
            .id(partyStatementId)
            .madeBy(party)
            .offer(offer)
            .build());
    }

    public void accept(MadeBy party, String partyStatementId) {
        assertOfferCanBeResponded(party);
        addAcceptation(party, partyStatementId);
    }

    public void addAcceptation(MadeBy party, String partyStatementId) {
        partyStatements.add(PartyStatement.builder().type(StatementType.ACCEPTATION)
            .id(partyStatementId)
            .madeBy(party).build());
    }

    public void acceptCourtDetermination(MadeBy party, String partyStatementId) {
        assertOfferCanBeAccepted();
        partyStatements.add(PartyStatement.builder().type(StatementType.ACCEPTATION)
            .id(partyStatementId)
            .madeBy(party).build());
    }

    public void reject(MadeBy party, String partyStatementId) {
        assertOfferCanBeRejected(party);
        addRejection(party, partyStatementId);
    }

    public void addRejection(MadeBy party, String partyStatementId) {
        partyStatements.add(PartyStatement.builder().type(StatementType.REJECTION)
            .id(partyStatementId)
            .madeBy(party).build());
    }

    public void countersign(MadeBy party, String partyStatementId) {
        assertOfferHasBeenAcceptedByOtherParty(party);
        addCounterSignature(party, partyStatementId);
    }

    public void addCounterSignature(MadeBy party, String partyStatementId) {
        partyStatements.add(PartyStatement.builder().type(StatementType.COUNTERSIGNATURE)
            .id(partyStatementId)
            .madeBy(party).build());
    }

    @JsonIgnore
    public PartyStatement getLastStatement() {
        if (partyStatements.isEmpty()) {
            throw new IllegalSettlementStatementException(NO_STATEMENTS_MADE);
        }
        return partyStatements.get(partyStatements.size() - 1);
    }

    @JsonIgnore
    public PartyStatement getLastStatementOfType(StatementType statementType) {
        if (partyStatements.isEmpty()) {
            throw new IllegalSettlementStatementException(NO_STATEMENTS_MADE);
        }

        List<PartyStatement> tmpList = new ArrayList<>(partyStatements);
        Collections.reverse(tmpList);

        return tmpList.stream()
            .filter((partyStatement -> partyStatement.getType() == statementType))
            .findFirst()
            .orElseThrow(() -> new IllegalSettlementStatementException("No statements with an offer found"));

    }

    @JsonIgnore
    public boolean isSettlementThroughAdmissions() {
        return getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException("Last offer statement is missing an offer"))
            .getPaymentIntention()
            .isPresent();
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

        assertOfferCanBeAccepted();
    }

    private void assertOfferCanBeRejected(MadeBy party) {
        assertOfferCanBeMadeBy(party);

        if (!lastStatementIsOffer() && !lastStatementIsAcceptationNotBy(party)) {
            throw new IllegalSettlementStatementException(
                format("Last statement was: %s, offer or acceptation expected.",
                    getLastStatement().getType().name().toLowerCase())
            );
        }
    }

    private void assertOfferCanBeAccepted() {
        if (!lastStatementIsOffer()) {
            throw new IllegalSettlementStatementException(
                format("Last statement was: %s , offer expected.", getLastStatement().getType().name().toLowerCase())
            );
        }
    }

    private void assertOfferHasBeenAcceptedByOtherParty(MadeBy party) {
        if (!lastStatementIsAcceptationNotBy(party)) {
            throw new IllegalSettlementStatementException(
                format("Last statement was: %s , offer acceptation expected.",
                    getLastStatement().getType().name().toLowerCase())
            );
        }
    }

    private boolean lastStatementIsAnOfferMadeBy(MadeBy madeBy) {
        return lastStatementIsOffer() && getLastStatement().getMadeBy().equals(madeBy);
    }

    private boolean lastStatementIsOffer() {
        return getLastStatement().getType().equals(StatementType.OFFER);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean lastStatementIsAcceptationNotBy(MadeBy madeBy) {
        PartyStatement lastStatement = getLastStatement();
        return lastStatement.getType().equals(StatementType.ACCEPTATION)
            && !lastStatement.getMadeBy().equals(madeBy);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
