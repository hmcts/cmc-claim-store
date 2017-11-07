package uk.gov.hmcts.cmc.claimstore.events;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;

import java.util.Objects;

public class OfferAcceptedEvent {

    private final Claim claim;
    private final MadeBy party;

    public OfferAcceptedEvent(final Claim claim, final MadeBy party) {
        this.claim = claim;
        this.party = party;
    }

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

        final OfferAcceptedEvent that = (OfferAcceptedEvent) other;
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
