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

    public boolean isAddressEqual() {
        return claim.getClaimData().getDefendant().getAddress().equals(
            defendant.getAddress()
        );
    }

    public boolean isCorrespondenceAddressEqual() {
        return claim.getClaimData().getDefendant().getServiceAddress().equals(
            defendant.getCorrespondenceAddress()
        );
    }

    //there's no phoneNumber in claimant
    public boolean isPhoneNumberEqual() {
        return defendant.getPhoneNumber() == null;
    }

    public boolean isEqual() {
        return this.isAddressEqual() && this.isCorrespondenceAddressEqual() && this.isPhoneNumberEqual();
    }
}
