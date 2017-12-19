package uk.gov.hmcts.cmc.claimstore.events.response;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

public class DefendantResponseEvent {
    private final String userEmail;
    private final Claim claim;

    public DefendantResponseEvent(Claim claim) {
        this.userEmail = claim.getDefendantEmail();
        this.claim = claim;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefendantResponseEvent that = (DefendantResponseEvent) obj;
        return Objects.equals(userEmail, that.userEmail)
            && Objects.equals(claim, that.claim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, claim);
    }
}
