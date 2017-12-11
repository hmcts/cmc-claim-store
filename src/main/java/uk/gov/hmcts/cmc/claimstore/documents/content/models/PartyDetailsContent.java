package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.domain.models.Address;

public class PartyDetailsContent {

    private final String type;
    private final String fullName;
    private final boolean nameAmended;
    private final String businessName;
    private final String contactPerson;
    private final Address address;
    private final boolean addressAmended;
    private final Address correspondenceAddress;
    private final String dateOfBirth;
    private final String email;

    public PartyDetailsContent(
        String type,
        String fullName,
        Boolean nameAmended,
        String businessName,
        String contactPerson,
        Address address,
        Boolean addressAmended,
        Address correspondenceAddress,
        String dateOfBirth,
        String email
    ) {
        this.type = type;
        this.fullName = fullName;
        this.nameAmended = nameAmended;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.address = address;
        this.addressAmended = addressAmended;
        this.correspondenceAddress = correspondenceAddress;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean getNameAmended() {
        return nameAmended;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public Address getAddress() {
        return address;
    }

    public boolean getAddressAmended() {
        return addressAmended;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }
}
