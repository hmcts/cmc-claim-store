package uk.gov.hmcts.cmc.rpa.mapper;


import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.rpa.domain.Address;

@Component("rpaAddressMapper")
public class AddressMapper {
    public Address to(uk.gov.hmcts.cmc.domain.models.Address address) {
        return Address
            .builder()
            .line1(address.getLine1())
            .line2(address.getLine2())
            .line3(address.getLine3())
            .city(address.getCity())
            .postcode(address.getPostcode())
            .build();
    }
}
