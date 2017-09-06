package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.models.Claim;

public class RepresentedClaimIssuedEvent {

    private final Claim claim;

    public RepresentedClaimIssuedEvent(Claim claim) {
        this.claim = claim;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getRepresentativeEmail() {
        return claim.getSubmitterEmail();
    }

}
