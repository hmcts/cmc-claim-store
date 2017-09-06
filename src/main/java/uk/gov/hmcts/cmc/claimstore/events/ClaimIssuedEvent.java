package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Objects;

public class ClaimIssuedEvent {
    private final String submitterEmail;
    private final Claim claim;
    private final String pin;

    public ClaimIssuedEvent(final Claim claim, final String pin) {
        this.submitterEmail = claim.getSubmitterEmail();
        this.claim = claim;
        this.pin = pin;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getPin() {
        return pin;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final ClaimIssuedEvent that = (ClaimIssuedEvent) other;
        return Objects.equals(submitterEmail, that.submitterEmail)
            && Objects.equals(claim, that.claim)
            && Objects.equals(pin, that.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitterEmail, claim, pin);
    }

    @Override
    public String toString() {
        return "ClaimIssuedEvent{"
            + "submitterEmail='" + submitterEmail + '\''
            + ", claim='" + claim + '\''
            + '}';
    }
}
