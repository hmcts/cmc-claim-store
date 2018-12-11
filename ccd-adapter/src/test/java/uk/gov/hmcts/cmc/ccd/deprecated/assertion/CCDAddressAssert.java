package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Objects;

public class CCDAddressAssert extends AbstractAssert<CCDAddressAssert, CCDAddress> {

    public CCDAddressAssert(CCDAddress actual) {
        super(actual, CCDAddressAssert.class);
    }

    public CCDAddressAssert isEqualTo(Address address) {
        isNotNull();

        if (!Objects.equals(actual.getLine1(), address.getLine1())) {
            failWithMessage("Expected CCDAddress.line1 to be <%s> but was <%s>",
                address.getLine1(), actual.getLine1());
        }

        if (!Objects.equals(actual.getLine2(), address.getLine2())) {
            failWithMessage("Expected CCDAddress.line2 to be <%s> but was <%s>",
                address.getLine2(), actual.getLine2());
        }

        if (!Objects.equals(actual.getLine3(), address.getLine3())) {
            failWithMessage("Expected CCDAddress.line3 to be <%s> but was <%s>",
                address.getLine3(), actual.getLine3());
        }

        if (!Objects.equals(actual.getCity(), address.getCity())) {
            failWithMessage("Expected CCDAddress.city to be <%s> but was <%s>",
                address.getCity(), actual.getCity());
        }

        if (!Objects.equals(actual.getPostcode(), address.getPostcode())) {
            failWithMessage("Expected CCDAddress.postcode to be <%s> but was <%s>",
                address.getPostcode(), actual.getPostcode());
        }

        return this;
    }

}
