package uk.gov.hmcts.cmc.claimstore.events;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.util.Objects;

/**
 * OfferAcceptedEvent and OfferRejectedEvent classes have the same fields, but we want to be able to distinguish
 * action that should be taken on each event.
 */
public abstract class OfferRespondedEvent {

    protected Claim claim;
    protected MadeBy party;

    public Claim getClaim() {
        return claim;
    }

    public MadeBy getParty() {
        return party;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final OfferRespondedEvent that = (OfferRespondedEvent) other;
        return Objects.equals(claim, that.claim) && Objects.equals(party, that.party);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, party);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
