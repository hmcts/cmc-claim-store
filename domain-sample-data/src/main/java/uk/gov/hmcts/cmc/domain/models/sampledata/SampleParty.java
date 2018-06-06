package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SampleParty {

    private String name = "John Rambo";
    private String businessName = "Trading as name";
    private String contactPerson = "Steven Seagal";
    private Address address = SampleAddress.validDefaults();
    private Address correspondenceAddress = SampleAddress.validDefaults();
    private String title = "Dr.";
    private String mobilePhone = "07873727165";
    private LocalDate dateOfBirth = LocalDate.of(1968, 1, 2);
    private Representative representative = SampleRepresentative.builder()
        .build();
    private String companiesHouseNumber;

    public static SampleParty builder() {
        return new SampleParty();
    }

    public SampleParty withName(String name) {
        this.name = name;
        return this;
    }

    public SampleParty withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public SampleParty withContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    public SampleParty withBusinessName(String businessName) {
        this.businessName = businessName;
        return this;
    }

    public SampleParty withAddress(Address address) {
        this.address = address;
        return this;
    }

    public SampleParty withCorrespondenceAddress(Address correspondenceAddress) {
        this.correspondenceAddress = correspondenceAddress;
        return this;
    }

    public SampleParty withMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
        return this;
    }

    public SampleParty withRepresentative(Representative representative) {
        this.representative = representative;
        return this;
    }

    public SampleParty withTitle(String title) {
        this.title = title;
        return this;
    }

    public SampleParty withCompaniesHouseNumber(String companiesHouseNumber) {
        this.companiesHouseNumber = companiesHouseNumber;
        return this;
    }

    public Party party() {
        return new Individual(name, address, correspondenceAddress, mobilePhone, representative, dateOfBirth);
    }

    public Individual individual() {
        return new Individual(name, address, correspondenceAddress, mobilePhone, representative, dateOfBirth);
    }

    public Individual individualWithAddress(Address address) {
        return new Individual(name, address, correspondenceAddress, mobilePhone, representative, dateOfBirth);
    }

    public List<Party> individualDetails(int count) {
        List<Party> individualDetailsList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            individualDetailsList.add(
                new Individual(name, address, correspondenceAddress, mobilePhone, representative, dateOfBirth)
            );
        }
        return individualDetailsList;
    }

    public SoleTrader soleTrader() {
        return new SoleTrader(name, address, correspondenceAddress, mobilePhone, representative, title, businessName);
    }

    public Company company() {
        return new Company(name, address, correspondenceAddress, mobilePhone, representative, contactPerson);
    }

    public Organisation organisation() {
        return new Organisation(name, address, correspondenceAddress, mobilePhone, representative, contactPerson,
            companiesHouseNumber);
    }

}
