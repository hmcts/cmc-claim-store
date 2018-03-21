package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;

public class PartyDetailsContent {

    private final String type;
    private final String fullName;
    private final boolean nameAmended;
    private final String businessName;
    private final String contactPerson;
    private final Address address;
    private final boolean addressAmended;
    private final Address correspondenceAddress;
    private final String mobilePhone;
    private final String dateOfBirth;
    private final String email;
    private final List<TimelineEvent> events;

    private final List<EvidenceContent> evidences;

    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public PartyDetailsContent(
        String type,
        String fullName,
        Boolean nameAmended,
        String businessName,
        String contactPerson,
        Address address,
        Boolean addressAmended,
        Address correspondenceAddress,
        String mobilePhone,
        String dateOfBirth,
        String email,
        List<TimelineEvent> events,
        List<EvidenceContent> evidences
    ) {
        this.type = type;
        this.fullName = fullName;
        this.nameAmended = nameAmended;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.address = address;
        this.addressAmended = addressAmended;
        this.correspondenceAddress = correspondenceAddress;
        this.mobilePhone = mobilePhone;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.events = events;
        this.evidences = evidences;
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

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public List<TimelineEvent> getEvents() {
        return events;
    }

    public List<EvidenceContent> getEvidences() {
        return evidences;
    }

}
