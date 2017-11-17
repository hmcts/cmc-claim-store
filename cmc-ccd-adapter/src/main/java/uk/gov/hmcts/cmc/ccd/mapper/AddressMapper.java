package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;

@Component
public class AddressMapper implements Mapper<CCDAddress, Address> {

    @Override
    public CCDAddress to(Address address) {
        return CCDAddress
            .builder()
            .line1(address.getLine1())
            .line2(address.getLine2())
            .city(address.getCity())
            .postcode(address.getPostcode())
            .build();
    }

    @Override
    public Address from(CCDAddress address) {
        return new Address(address.getLine1(), address.getLine2(),
            address.getCity(), address.getPostcode());
    }
}
