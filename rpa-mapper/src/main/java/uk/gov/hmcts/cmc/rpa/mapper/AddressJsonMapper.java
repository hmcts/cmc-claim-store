package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
public class AddressJsonMapper {

    public JsonObject mapAddress(Address address) {
        return new NullAwareJsonObjectBuilder()
            .add("line1", address.getLine1())
            .add("line2", address.getLine2())
            .add("line3", address.getLine3())
            .add("city", address.getCity())
            .add("postcode", address.getPostcode())
            .build();
    }

}
