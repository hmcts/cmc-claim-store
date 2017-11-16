package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.domain.models.Address;

public class AddressMapper {

    uk.gov.hmcts.cmc.ccd.domain.Address toCCD(Address address) {

        uk.gov.hmcts.cmc.ccd.domain.Address ccdAddress = uk.gov.hmcts.cmc.ccd.domain.Address
            .builder()
            .line1(address.getLine1())
            .line2(address.getLine2())
            .city(address.getCity())
            .postcode(address.getPostcode())
            .build();

        return ccdAddress;
    }

    Address toCMC(uk.gov.hmcts.cmc.ccd.domain.Address address) {
        Address cmcAddress = new Address(address.getLine1(), address.getLine2(),
            address.getCity(), address.getPostcode());
        return cmcAddress;
    }
}
