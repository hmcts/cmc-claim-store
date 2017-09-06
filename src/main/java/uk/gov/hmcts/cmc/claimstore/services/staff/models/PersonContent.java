package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;

public class PersonContent {

    private final String fullName;
    private final Address address;
    private final Address correspondenceAddress;
    private final String email;

    public PersonContent(String fullName, Address address, Address correspondenceAddress) {
        this(fullName, address, correspondenceAddress, null);
    }

    public PersonContent(String fullName, Address address, Address correspondenceAddress, String email) {
        this.fullName = fullName;
        this.address = address;
        this.correspondenceAddress = correspondenceAddress;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public String getEmail() {
        return email;
    }
}
