package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;

import java.util.Objects;

public class DefaultJudgmentSubmittedEvent {

    private final DefaultJudgment defaultJudgment;
    private final Claim claim;

    public DefaultJudgmentSubmittedEvent(final DefaultJudgment defaultJudgment, final Claim claim) {
        this.defaultJudgment = defaultJudgment;
        this.claim = claim;
    }

    public DefaultJudgment getDefaultJudgment() {
        return defaultJudgment;
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

        final DefaultJudgmentSubmittedEvent that = (DefaultJudgmentSubmittedEvent) other;
        return Objects.equals(defaultJudgment, that.defaultJudgment)
            && Objects.equals(claim, that.claim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultJudgment, claim);
    }

    @Override
    public String toString() {
        return "DefaultJudgmentSubmitted "
            + "defaultJudgment= " + defaultJudgment
            + ", claim='" + claim;
    }
}
