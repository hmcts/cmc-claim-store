package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SampleTheirDetails {

    public static final String DEFENDANT_EMAIL = "j.smith@example.com";

    private String name = "Dr. John Smith";
    private String firstName = "John";
    private String lastName = "Smith";
    private Address address = SampleAddress.builder().build();
    private Address claimantProvidedAddress = SampleAddress.builder().build();
    private String email = DEFENDANT_EMAIL;
    private String contactPerson = "Arnold Schwarzenegger";
    private String businessName = "Sole Trading & Sons";
    private String title = "Dr.";
    private Representative representative;
    private String companiesHouseNumber;
    private Address serviceAddress;
    private LocalDate dateOfBirth;
    private String collectionId = "3d0bc933-0d46-4564-94bd-79e6e69b838b";
    private String phoneNumber;

    public static SampleTheirDetails builder() {
        return new SampleTheirDetails();
    }

    public SampleTheirDetails withPhone(String phone) {
        this.phoneNumber = phone;
        return this;
    }

    public SampleTheirDetails withName(String name) {
        this.name = name;
        return this;
    }

    public SampleTheirDetails withAddress(Address address) {
        this.address = address;
        return this;
    }

    public SampleTheirDetails withEmail(String email) {
        this.email = email;
        return this;
    }

    public SampleTheirDetails withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public SampleTheirDetails withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public SampleTheirDetails withTitle(String title) {
        this.title = title;
        return this;
    }

    public SampleTheirDetails withRepresentative(Representative representative) {
        this.representative = representative;
        return this;
    }

    public SampleTheirDetails withServiceAddress(Address serviceAddress) {
        this.serviceAddress = serviceAddress;
        return this;
    }

    public SampleTheirDetails withContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    public SampleTheirDetails withBusinessName(String businessName) {
        this.businessName = businessName;
        return this;
    }

    public SampleTheirDetails withCompaniesHouseNumber(String companiesHouseNumber) {
        this.companiesHouseNumber = companiesHouseNumber;
        return this;
    }

    public SampleTheirDetails withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public SampleTheirDetails withCollectionId(String collectionId) {
        this.collectionId = collectionId;
        return this;
    }

    public SampleTheirDetails withClaimantProvidedAddress(Address address) {
        this.claimantProvidedAddress = address;
        return this;
    }

    public TheirDetails partyDetails() {
        return new IndividualDetails(collectionId, name, title, firstName, lastName,
            address, email, representative, serviceAddress, dateOfBirth, claimantProvidedAddress, phoneNumber);
    }

    public IndividualDetails individualDetails() {
        return new IndividualDetails(collectionId, name, title, firstName, lastName,
            address, email, representative, serviceAddress, dateOfBirth, claimantProvidedAddress, phoneNumber);
    }

    public List<TheirDetails> individualDetails(int count) {
        List<TheirDetails> individualDetailsList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            individualDetailsList.add(
                new IndividualDetails(collectionId, name, title, firstName, lastName,
                    address, email, representative, serviceAddress, dateOfBirth, claimantProvidedAddress, phoneNumber)
            );
        }
        return individualDetailsList;
    }

    public CompanyDetails companyDetails() {
        return new CompanyDetails(collectionId, name, address, email, representative, serviceAddress, contactPerson,
            claimantProvidedAddress, phoneNumber);
    }

    public OrganisationDetails organisationDetails() {
        return new OrganisationDetails(collectionId, name, address, email, representative, serviceAddress,
            contactPerson, companiesHouseNumber, claimantProvidedAddress, phoneNumber);
    }

    public SoleTraderDetails soleTraderDetails() {
        return new SoleTraderDetails(collectionId, name, firstName, lastName, address, email,
            representative, serviceAddress, claimantProvidedAddress, title, businessName, phoneNumber);
    }

}
