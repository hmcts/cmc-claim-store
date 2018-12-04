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

    private String name = "John Smith";
    private Address address = SampleAddress.builder().build();
    private String email = DEFENDANT_EMAIL;
    private String contactPerson = "Arnold Schwarzenegger";
    private String businessName = "Sole Trading & Sons";
    private String title = "Dr.";
    private Representative representative = SampleRepresentative.builder().build();
    private String companiesHouseNumber = "12345";
    private Address serviceAddress = SampleAddress.builder().build();
    private LocalDate dateOfBirth = LocalDate.of(1970, 10,10);

    public static SampleTheirDetails builder() {
        return new SampleTheirDetails();
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

    public TheirDetails partyDetails() {
        return new IndividualDetails(name, address, email, representative, serviceAddress, dateOfBirth);
    }

    public IndividualDetails individualDetails() {
        return new IndividualDetails(name, address, email, representative, serviceAddress, dateOfBirth);
    }

    public List<TheirDetails> individualDetails(int count) {
        List<TheirDetails> individualDetailsList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            individualDetailsList.add(
                new IndividualDetails(name, address, email, representative, serviceAddress, dateOfBirth)
            );
        }
        return individualDetailsList;
    }

    public CompanyDetails companyDetails() {
        return new CompanyDetails(name, address, email, representative, serviceAddress, contactPerson);
    }

    public OrganisationDetails organisationDetails() {
        return new OrganisationDetails(name, address, email, representative, serviceAddress,
            contactPerson, companiesHouseNumber);
    }

    public SoleTraderDetails soleTraderDetails() {
        return new SoleTraderDetails(name, address, email, representative, serviceAddress, title, businessName);
    }

}
