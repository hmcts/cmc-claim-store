package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;


@Component
@SuppressWarnings({"LineLength"})
public class MoreTimeRequestedJsonMapper {

    private final AddressJsonMapper addressJsonMapper;

    @Autowired
    public MoreTimeRequestedJsonMapper(AddressJsonMapper addressJsonMapper) {
        this.addressJsonMapper = addressJsonMapper;
    }

    public JsonObject map(Claim claim) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("responseDeadline", DateFormatter.format(claim.getResponseDeadline()))
            .add("addressAmended", RPAMapperHelper.isAddressAmended(response.getDefendant(), claim.getClaimData().getDefendant()))
            .add("address", addressJsonMapper.map(response.getDefendant().getAddress()))
            .add("correspondenceAddress", response.getDefendant().getCorrespondenceAddress().map(addressJsonMapper::map).orElse(null))
            .build();
    }

}
