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
            .addressLine1(address.getLine1())
            .addressLine2(address.getLine2())
            .addressLine3(address.getLine3())
            .postTown(address.getCity())
            .postCode(address.getPostcode())
            .county(address.getCounty())
            .build();
    }

    @Override
    public Address from(CCDAddress address) {
        if (address == null) {
            return null;
        }

        return new Address(address.getAddressLine1(), address.getAddressLine2(), address.getAddressLine3(),
            address.getPostTown(), address.getCounty(), address.getPostCode());
    }
}
