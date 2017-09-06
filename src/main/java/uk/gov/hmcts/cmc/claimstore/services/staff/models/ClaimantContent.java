package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;

public class ClaimantContent extends PersonContent {

    private final String email;

    public ClaimantContent(String fullName, Address address, Address correspondenceAddress, String email) {
        super(fullName, address, correspondenceAddress);
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }

}
