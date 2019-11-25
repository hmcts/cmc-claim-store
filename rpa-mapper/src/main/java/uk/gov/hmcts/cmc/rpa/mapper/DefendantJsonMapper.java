package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonCollectors;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper.isAddressAmended;

@Component
@SuppressWarnings({"LineLength"})
public class DefendantJsonMapper {

    @Autowired
    private final AddressJsonMapper addressMapper;

    public DefendantJsonMapper(AddressJsonMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public JsonArray map(List<TheirDetails> defendants) {
        return defendants.stream().map(defendant -> new NullAwareJsonObjectBuilder()
            .add("type", defendant.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendant.getName())
            .add("address", addressMapper.map(defendant.getAddress()))
            .add("correspondenceAddress", defendant.getServiceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendant.getEmail().orElse(null))
            .add("businessName", extractOptionalFromSubclass(defendant, SoleTraderDetails.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendant, HasContactPerson.class, HasContactPerson::getContactPerson))
            .build()).collect(JsonCollectors.toJsonArray());
    }

    public JsonObject map(Party defendantFromResponse, TheirDetails defendantFromClaim, String defendantsEmail) {
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("type", defendantFromResponse.getClass().getSimpleName())
            .add("name", defendantFromResponse.getName())
            .add("address", addressMapper.map(defendantFromResponse.getAddress()))
            .add("correspondenceAddress", defendantFromResponse.getCorrespondenceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendantsEmail)
            .add("addressAmended", isAddressAmended(defendantFromResponse, defendantFromClaim))
            .add("businessName", extractOptionalFromSubclass(defendantFromResponse, SoleTrader.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendantFromResponse, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("dateOfBirth", extractFromSubclass(defendantFromResponse, Individual.class, individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber", extractFromSubclass(defendantFromResponse, Party.class, party -> party.getPhone().orElse(null)));

        return jsonObjectBuilder.build();
    }
}
