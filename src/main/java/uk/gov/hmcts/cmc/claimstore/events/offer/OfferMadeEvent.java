package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class OfferMadeEvent {

    private final Claim claim;

    public OfferMadeEvent(Claim claim) {
        this.claim = claim;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        OfferMadeEvent that = (OfferMadeEvent) other;
        return Objects.equals(claim, that.claim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
