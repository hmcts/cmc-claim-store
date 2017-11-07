package uk.gov.hmcts.cmc.claimstore.models.offers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PartyStatement {

    private StatementType type;
    private MadeBy madeBy;
    private Offer offer;

    public PartyStatement() {
        // default contractor required by Jackson
    }

    public PartyStatement(StatementType type, MadeBy madeBy, Offer offer) {
        this.type = type;
        this.madeBy = madeBy;
        this.offer = offer;
    }

    public PartyStatement(StatementType type, MadeBy madeBy) {
        this.type = type;
        this.madeBy = madeBy;
    }

    public StatementType getType() {
        return type;
    }

    public MadeBy getMadeBy() {
        return madeBy;
    }

    public Optional<Offer> getOffer() {
        return Optional.ofNullable(offer);
    }
}
