package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Optional;

public class CCDAddressAssert extends CustomAssert<CCDAddressAssert, CCDAddress> {

    CCDAddressAssert(CCDAddress actual) {
        super("CCDAddress", actual, CCDAddressAssert.class);
    }

    public CCDAddressAssert isEqualTo(Address expected) {
        isNotNull();

        compare("line1",
            expected.getLine1(),
            Optional.ofNullable(actual.getAddressLine1()));

        compare("line2",
            expected.getLine2(),
            Optional.ofNullable(actual.getAddressLine2()));

        compare("line3",
            expected.getLine3(),
            Optional.ofNullable(actual.getAddressLine3()));

        compare("city",
            expected.getCity(),
            Optional.ofNullable(actual.getPostTown()));

        compare("postcode",
            expected.getPostcode(),
            Optional.ofNullable(actual.getPostCode()));

        return this;
    }

}
