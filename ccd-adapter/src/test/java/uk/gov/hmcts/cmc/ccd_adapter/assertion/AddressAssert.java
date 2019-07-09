package uk.gov.hmcts.cmc.ccd-adapter.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Objects;

public class AddressAssert extends AbstractAssert<AddressAssert, Address> {

    public AddressAssert(Address actual) {
        super(actual, AddressAssert.class);
    }

    public AddressAssert isEqualTo(CCDAddress ccdAddress) {
        isNotNull();

        if (!Objects.equals(actual.getLine1(), ccdAddress.getAddressLine1())) {
            failWithMessage("Expected Address.line1 to be <%s> but was <%s>",
                ccdAddress.getAddressLine1(), actual.getLine1());
        }

        if (!Objects.equals(actual.getLine2(), ccdAddress.getAddressLine2())) {
            failWithMessage("Expected Address.line2 to be <%s> but was <%s>",
                ccdAddress.getAddressLine2(), actual.getLine2());
        }

        if (!Objects.equals(actual.getLine3(), ccdAddress.getAddressLine3())) {
            failWithMessage("Expected Address.line3 to be <%s> but was <%s>",
                ccdAddress.getAddressLine3(), actual.getLine3());
        }

        if (!Objects.equals(actual.getCity(), ccdAddress.getPostTown())) {
            failWithMessage("Expected Address.city to be <%s> but was <%s>",
                ccdAddress.getPostTown(), actual.getCity());
        }

        if (!Objects.equals(actual.getPostcode(), ccdAddress.getPostCode())) {
            failWithMessage("Expected Address.postcode to be <%s> but was <%s>",
                ccdAddress.getPostCode(), actual.getPostcode());
        }

        return this;
    }

}
