package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;

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
}
