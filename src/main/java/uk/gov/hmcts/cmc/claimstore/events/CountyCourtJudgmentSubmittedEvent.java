package uk.gov.hmcts.cmc.claimstore.events;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Objects;

public class CountyCourtJudgmentSubmittedEvent {

    private final Claim claim;

    public CountyCourtJudgmentSubmittedEvent(final Claim claim) {
        this.claim = claim;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final CountyCourtJudgmentSubmittedEvent that = (CountyCourtJudgmentSubmittedEvent) other;
        return Objects.equals(claim, that.claim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
