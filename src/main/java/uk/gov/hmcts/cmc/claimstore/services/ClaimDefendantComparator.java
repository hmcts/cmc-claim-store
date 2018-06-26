package uk.gov.hmcts.cmc.claimstore.services;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PartyContactDetails;

public class ClaimDefendantComparator {

    private Claim claim;
    private PartyContactDetails defendant;

    public ClaimDefendantComparator(Claim claim, PartyContactDetails defendant) {
        this.claim = claim;
        this.defendant = defendant;
    }

    public boolean isDefendantAddressEqual() {
        return claim.getClaimData().getDefendant().getAddress().equals(
            defendant.getAddress()
        );
    }

    public boolean isDefendantCorrespondenceAddressEqual() {
        return claim.getClaimData().getDefendant().getServiceAddress().equals(
            defendant.getCorrespondenceAddress()
        );
    }
}
