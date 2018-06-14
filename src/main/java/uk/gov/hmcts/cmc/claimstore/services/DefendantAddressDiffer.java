package uk.gov.hmcts.cmc.claimstore.services;

import de.danielbechler.diff.node.DiffNode;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import de.danielbechler.diff.ObjectDifferBuilder;

import java.util.Optional;

public class DefendantAddressDiffer {

    private Claim claim;
    private Response response;

    // that class could be more generic if defendant details in Claim and Response shared the same interface
    // now it's TheirDetails vs Party, todo add 'hasTwoAddresses' interface
    public DefendantAddressDiffer(Claim claim, Response response) {
        this.claim = claim;
        this.response = response;
    }

    private DiffNode getCorrespondenceAddressDiff() {
        Optional<Address> correspondenceAddressInClaim = claim.getClaimData().getDefendant().getServiceAddress();
        Optional<Address> correspondenceAddressInResponse = response.getDefendant().getCorrespondenceAddress();

        DiffNode diff = ObjectDifferBuilder.buildDefault().compare(
            correspondenceAddressInClaim, correspondenceAddressInResponse
        );

        return diff;
    }

    private DiffNode getAddressDiff() {
        Address addressInClaim = claim.getClaimData().getDefendant().getAddress();
        Address addressInResponse = response.getDefendant().getAddress();

        if(addressInClaim.equals(addressInResponse)) {
            return null;
        }

        DiffNode diff = ObjectDifferBuilder.buildDefault().compare(
            addressInClaim, addressInResponse
        );

        return diff;
    }

    public AddressDiff getDiff() {
        AddressDiff diff = new AddressDiff();

        diff.setAddressDiff(getAddressDiff());
        diff.setCorrespondenceAddressDiff(getCorrespondenceAddressDiff());

        return diff;
    }
}
