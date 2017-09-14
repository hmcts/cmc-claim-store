package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Objects;

public class RepresentedClaimIssuedEvent {

    private final Claim claim;
    private final String representativeName;
    private final String representativeEmail;

    public RepresentedClaimIssuedEvent(Claim claim, final String submitterName) {
        this.claim = claim;
        this.representativeName = submitterName;
        this.representativeEmail = claim.getSubmitterEmail();
    }

    public Claim getClaim() {
        return claim;
    }

    public String getRepresentativeEmail() {
        return representativeEmail;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final RepresentedClaimIssuedEvent that = (RepresentedClaimIssuedEvent) obj;
        return Objects.equals(claim, that.claim)
            && Objects.equals(representativeName, that.representativeName)
            && Objects.equals(representativeEmail, that.representativeEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, representativeName, representativeEmail);
    }

    @Override
    public String toString() {
        return "RepresentedClaimIssuedEvent{"
            + "claim=" + claim
            + ", representativeName='" + representativeName + '\''
            + ", representativeEmail='" + representativeEmail + '\''
            + '}';
    }
}
