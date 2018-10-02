package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import uk.gov.hmcts.cmc.domain.models.Claim;


public class ClaimantSaysDefendantHasPaidInFullEvent {
    private final String defendantEmail;
    private final Claim claim;

    public ClaimantSaysDefendantHasPaidInFullEvent(Claim claim) {
        this.defendantEmail = claim.getDefendantEmail();
        this.claim = claim;
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    public Claim getClaim() {
        return claim;
    }
}

