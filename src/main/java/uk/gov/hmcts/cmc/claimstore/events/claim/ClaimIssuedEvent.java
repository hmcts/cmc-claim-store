package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class ClaimIssuedEvent {
    private final Claim claim;
    private final String submitterName;
    private final String authorisation;

    public ClaimIssuedEvent(Claim claim,
                            String submitterName,
                            String authorisation
    ) {
        this.claim = claim;
        this.submitterName = submitterName;
        this.authorisation = authorisation;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ClaimIssuedEvent that = (ClaimIssuedEvent) other;
        return Objects.equals(claim, that.claim) &&
            Objects.equals(submitterName, that.submitterName) &&
            Objects.equals(authorisation, that.authorisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, submitterName, authorisation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
