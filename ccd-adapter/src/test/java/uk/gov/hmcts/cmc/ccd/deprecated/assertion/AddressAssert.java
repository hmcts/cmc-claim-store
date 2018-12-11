package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Objects;

public class AddressAssert extends AbstractAssert<AddressAssert, Address> {

    public AddressAssert(Address actual) {
        super(actual, AddressAssert.class);
    }

    public AddressAssert isEqualTo(CCDAddress ccdAddress) {
        isNotNull();

        if (!Objects.equals(actual.getLine1(), ccdAddress.getLine1())) {
            failWithMessage("Expected Address.line1 to be <%s> but was <%s>",
                ccdAddress.getLine1(), actual.getLine1());
        }

        if (!Objects.equals(actual.getLine2(), ccdAddress.getLine2())) {
            failWithMessage("Expected Address.line2 to be <%s> but was <%s>",
                ccdAddress.getLine2(), actual.getLine2());
        }

        if (!Objects.equals(actual.getLine3(), ccdAddress.getLine3())) {
            failWithMessage("Expected Address.line3 to be <%s> but was <%s>",
                ccdAddress.getLine3(), actual.getLine3());
        }

        if (!Objects.equals(actual.getCity(), ccdAddress.getCity())) {
            failWithMessage("Expected Address.city to be <%s> but was <%s>",
                ccdAddress.getCity(), actual.getCity());
        }

        if (!Objects.equals(actual.getPostcode(), ccdAddress.getPostcode())) {
            failWithMessage("Expected Address.postcode to be <%s> but was <%s>",
                ccdAddress.getPostcode(), actual.getPostcode());
        }

        return this;
    }

}
