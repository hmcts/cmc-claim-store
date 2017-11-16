package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.domain.models.Address;

public class AddressMapper implements Mapper<uk.gov.hmcts.cmc.ccd.domain.Address, Address> {

    @Override
    public uk.gov.hmcts.cmc.ccd.domain.Address to(Address address) {
        return uk.gov.hmcts.cmc.ccd.domain.Address
            .builder()
            .line1(address.getLine1())
            .line2(address.getLine2())
            .city(address.getCity())
            .postcode(address.getPostcode())
            .build();
    }

    @Override
    public Address from(uk.gov.hmcts.cmc.ccd.domain.Address address) {
        return new Address(address.getLine1(), address.getLine2(),
            address.getCity(), address.getPostcode());
    }
}
