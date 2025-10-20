package uk.gov.hmcts.cmc.claimstore.events.response;

import uk.gov.hmcts.cmc.domain.models.Claim;
import java.time.LocalDate;
import java.util.Objects;

public class MoreTimeRequestedEvent {
    private final Claim claim;
    private final LocalDate newResponseDeadline;
    private final String defendantEmail;

    public MoreTimeRequestedEvent(Claim claim, LocalDate newResponseDeadline, String defendantEmail) {
        this.claim = claim;
        this.newResponseDeadline = newResponseDeadline;
        this.defendantEmail = defendantEmail;
    }

    public Claim getClaim() {
        return claim;
    }

    public LocalDate getNewResponseDeadline() {
        return newResponseDeadline;
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        MoreTimeRequestedEvent that = (MoreTimeRequestedEvent) other;
        return Objects.equals(claim, that.claim)
            && Objects.equals(newResponseDeadline, that.newResponseDeadline)
            && Objects.equals(defendantEmail, that.defendantEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, newResponseDeadline, defendantEmail);
    }

    @Override
    public String toString() {
        return "MoreTimeRequestedEvent{ claim='" + claim
            + "', newResponseDeadline = '" + newResponseDeadline + "'', defendantEmail = '" + defendantEmail + "'}";
    }
}
