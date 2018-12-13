package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

//@Component
public class AddressMapper implements Mapper<CCDAddress, Address> {

    @Override
    public CCDAddress to(Address address) {
        return CCDAddress
            .builder()
            .line1(address.getLine1())
            .line2(address.getLine2())
            .line3(address.getLine3())
            .city(address.getCity())
            .postcode(address.getPostcode())
            .build();
    }

    @Override
    public Address from(CCDAddress address) {
        if (address == null) {
            return null;
        }

        return new Address(address.getLine1(), address.getLine2(), address.getLine3(),
            address.getCity(), address.getPostcode());
    }
}
