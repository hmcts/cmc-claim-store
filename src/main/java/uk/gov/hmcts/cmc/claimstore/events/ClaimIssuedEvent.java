package uk.gov.hmcts.cmc.claimstore.events;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

public class ClaimIssuedEvent {
    private final String submitterEmail;
    private final Claim claim;
    private final String pin;
    private final String submitterName;
    private final String authorisation;

    public ClaimIssuedEvent(final Claim claim, final String pin,
                            final String submitterName, final String authorisation) {
        this.submitterEmail = claim.getSubmitterEmail();
        this.claim = claim;
        this.pin = pin;
        this.submitterName = submitterName;
        this.authorisation = authorisation;
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

    public String getSubmitterName() {
        return submitterName;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
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
            && Objects.equals(pin, that.pin)
            && Objects.equals(submitterName, that.submitterName)
            && Objects.equals(authorisation, that.authorisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitterEmail, claim, pin, submitterName, authorisation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
