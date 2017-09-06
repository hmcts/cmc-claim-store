package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;

import java.util.Objects;

public class DefendantResponseEvent {
    private final String userEmail;
    private final Claim claim;
    private DefendantResponse response;

    public DefendantResponseEvent(final Claim claim, final DefendantResponse response) {
        this.userEmail = response.getDefendantEmail();
        this.claim = claim;
        this.response = response;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Claim getClaim() {
        return claim;
    }

    public DefendantResponse getDefendantResponse() {
        return response;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final DefendantResponseEvent that = (DefendantResponseEvent) other;
        return Objects.equals(userEmail, that.userEmail)
            && Objects.equals(claim, that.claim)
            && Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, claim, response);
    }
}
