package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonCollectors;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class DefendantJsonMapper {

    @Autowired
    private final AddressJsonMapper addressMapper;

    public DefendantJsonMapper(AddressJsonMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public JsonArray mapDefendants(List<TheirDetails> defendants) {
        return defendants.stream().map(defendant -> addValues(defendant).build()).collect(JsonCollectors.toJsonArray());
    }

    public JsonArray mapDefendantsForDefenceResponse(List<TheirDetails> defendants, Party ownParty) {
        return defendants.stream()
            .map(defendant -> addValues(defendant).add("isAddressAmended", isAddressAmended(defendant, ownParty)).build())
            .collect(JsonCollectors.toJsonArray());
    }

    private String isAddressAmended(TheirDetails defendant, Party respondentParty) {
        if (!defendant.getAddress().equals(respondentParty.getAddress())) {
            return "yes";
        }
        return "no";
    }

    private JsonObjectBuilder addValues(TheirDetails defendant) {
        return new NullAwareJsonObjectBuilder()
            .add("type", defendant.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendant.getName())
            .add("address", addressMapper.mapAddress(defendant.getAddress()))
            .add("correspondenceAddress", defendant.getServiceAddress().map(addressMapper::mapAddress).orElse(null))
            .add("emailAddress", defendant.getEmail().orElse(null))
            .add("businessName", extractOptionalFromSubclass(defendant, SoleTraderDetails.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendant, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendant, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber));
    }
}
