package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

public class Assertions {

    private Assertions() {
    }

    public static AddressAssert assertThat(Address address) {
        return new AddressAssert(address);
    }

    public static CCDAddressAssert assertThat(CCDAddress ccdAddress) {
        return new CCDAddressAssert(ccdAddress);
    }

    public static ContactDetailsAssert assertThat(ContactDetails contactDetails) {
        return new ContactDetailsAssert(contactDetails);
    }

    public static CCDContactDetailsAssert assertThat(CCDContactDetails ccdContactDetails) {
        return new CCDContactDetailsAssert(ccdContactDetails);
    }

    public static RepresentativeAssert assertThat(CCDRepresentative ccdRepresentative) {
        return new RepresentativeAssert(ccdRepresentative);
    }

    public static IndividualAssert assertThat(Individual individual) {
        return new IndividualAssert(individual);
    }

    public static SoleTraderAssert assertThat(SoleTrader soleTrader) {
        return new SoleTraderAssert(soleTrader);
    }

    public static OrganisationAssert assertThat(Organisation organisation) {
        return new OrganisationAssert(organisation);
    }

    public static CompanyAssert assertThat(Company company) {
        return new CompanyAssert(company);
    }

    public static PartyAssert assertThat(Party party) {
        return new PartyAssert(party);
    }

    public static ClaimDataAssert assertThat(ClaimData claimData) {
        return new ClaimDataAssert(claimData);
    }

    public static CompanyDetailsAssert assertThat(CompanyDetails companyDetails) {
        return new CompanyDetailsAssert(companyDetails);
    }
}
