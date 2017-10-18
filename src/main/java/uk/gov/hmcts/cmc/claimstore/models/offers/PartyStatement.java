package uk.gov.hmcts.cmc.claimstore.models.offers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PartyStatement {

    private final StatementType type;
    private final MadeBy madeBy;
    private final Offer offer;

    public PartyStatement(StatementType type, MadeBy madeBy, Offer offer) {
        this.type = type;
        this.madeBy = madeBy;
        this.offer = offer;
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
