package uk.gov.hmcts.cmc.domain.models.offers;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class PartyStatement extends CollectionId {

    private final StatementType type;
    private final MadeBy madeBy;
    private final Offer offer;

    @Builder
    public PartyStatement(String id, StatementType type, MadeBy madeBy, Offer offer) {
        super(id);
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
