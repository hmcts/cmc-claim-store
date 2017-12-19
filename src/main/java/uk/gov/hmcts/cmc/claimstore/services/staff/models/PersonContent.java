package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.domain.models.Address;

public class PersonContent {

    private final String partyType;
    private final String fullName;
    private final Address address;
    private final Address correspondenceAddress;
    private final String email;
    private final String contactPerson;
    private final String businessName;

    public PersonContent(
        String partyType,
        String fullName,
        Address address,
        Address correspondenceAddress,
        String email,
        String contactPerson,
        String businessName
    ) {
        this.partyType = partyType;
        this.fullName = fullName;
        this.address = address;
        this.correspondenceAddress = correspondenceAddress;
        this.email = email;
        this.contactPerson = contactPerson;
        this.businessName = businessName;
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

    public String getPartyType() {
        return partyType;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getBusinessName() {
        return businessName;
    }
}
