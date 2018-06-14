package uk.gov.hmcts.cmc.claimstore.services;

import de.danielbechler.diff.node.DiffNode;

public class AddressDiff {
    private DiffNode addressDiff;
    private DiffNode correspondenceAddressDiff;

    public DiffNode getAddressDiff() {
        return addressDiff;
    }

    public void setAddressDiff(DiffNode addressDiff) {
        this.addressDiff = addressDiff;
    }

    public DiffNode getCorrespondenceAddressDiff() {
        return correspondenceAddressDiff;
    }

    public void setCorrespondenceAddressDiff(DiffNode correspondenceAddressDiff) {
        this.correspondenceAddressDiff = correspondenceAddressDiff;
    }

    public boolean isEmpty()
    {
        return addressDiff == null && correspondenceAddressDiff == null;
    }
}
