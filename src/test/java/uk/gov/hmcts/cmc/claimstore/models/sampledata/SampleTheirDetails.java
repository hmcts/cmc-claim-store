package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import java.util.ArrayList;
import java.util.List;

public class SampleTheirDetails {

    public static final String DEFENDANT_EMAIL = "j.smith@example.com";

    private String name = "John Smith";
    private Address address = SampleAddress.validDefaults();
    private String email = DEFENDANT_EMAIL;
    private String contactPerson = "Arnold Schwarzenegger";
    private String businessName = "Sole Trading & Sons";
    private String title = "Dr.";
    private Representative representative = SampleRepresentative.builder()
        .build();
    private String companiesHouseNumber;

    private Address serviceAddress = SampleAddress.validDefaults();

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

    public TheirDetails partyDetails() {
        return new IndividualDetails(name, address, email, representative, serviceAddress, title);
    }

    public IndividualDetails individualDetails() {
        return new IndividualDetails(name, address, email, representative, serviceAddress, title);
    }

    public List<TheirDetails> individualDetails(int count) {
        List<TheirDetails> individualDetailsList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            individualDetailsList.add(
                new IndividualDetails(name, address, email, representative, serviceAddress, title)
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
