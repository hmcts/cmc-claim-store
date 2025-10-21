package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Optional;

public class AddressAssert extends CustomAssert<AddressAssert, Address> {

    AddressAssert(Address actual) {
        super("Address", actual, AddressAssert.class);
    }

    public AddressAssert isEqualTo(CCDAddress expected) {
        isNotNull();

        compare("line1",
            expected.getAddressLine1(),
            Optional.ofNullable(actual.getLine1()));

        compare("line2",
            expected.getAddressLine2(),
            Optional.ofNullable(actual.getLine2()));

        compare("line3",
            expected.getAddressLine3(),
            Optional.ofNullable(actual.getLine3()));

        compare("city",
            expected.getPostTown(),
            Optional.ofNullable(actual.getCity()));

        compare("postCode",
            expected.getPostCode(),
            Optional.ofNullable(actual.getPostcode()));

        return this;
    }

}
