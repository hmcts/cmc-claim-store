package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;

public class DefendantDetailsContent {

    private final String fullName;
    private final Boolean nameAmended;
    private final Address address;
    private final Address correspondenceAddress;
    private final Boolean addressAmended;
    private final String dateOfBirth;
    private final String email;

    public DefendantDetailsContent(
        String fullName,
        Boolean nameAmended,
        Address address,
        Address correspondenceAddress,
        Boolean addressAmended,
        String dateOfBirth,
        String email) {
        this.fullName = fullName;
        this.nameAmended = nameAmended;
        this.address = address;
        this.correspondenceAddress = correspondenceAddress;
        this.addressAmended = addressAmended;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public Boolean getNameAmended() {
        return nameAmended;
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public Boolean getAddressAmended() {
        return addressAmended;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }
}
