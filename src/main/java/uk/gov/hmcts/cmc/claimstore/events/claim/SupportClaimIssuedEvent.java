package uk.gov.hmcts.cmc.claimstore.events.claim;

import uk.gov.hmcts.cmc.domain.models.Claim;

public class SupportClaimIssuedEvent {
    private final Claim claim;
    private final String authorisation;

    public SupportClaimIssuedEvent(Claim claim,
                            String authorisation
    ) {
        this.claim = claim;
        this.authorisation = authorisation;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getAuthorisation() {
        return authorisation;
    }
}
