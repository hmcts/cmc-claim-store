package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.List;
import javax.json.*;
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

    public JsonArray map(TheirDetails defendants){
        return Json.createArrayBuilder().add(addValues(defendants)).build();
    }

    public JsonArray map(Party ownParty, TheirDetails oppositeParty, String defendantsEmail) {

        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("type", ownParty.getClass().getSimpleName().replace("Details", ""))
            .add("name", ownParty.getName())
            .add("address", addressMapper.map(ownParty.getAddress()))
            .add("correspondenceAddress", ownParty.getCorrespondenceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendantsEmail)
            .add("addressAmended", isAddressAmended(ownParty, oppositeParty).name().toLowerCase())
            .add("businessName", extractOptionalFromSubclass(ownParty, SoleTraderDetails.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(ownParty, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(ownParty, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber));

        return Json.createArrayBuilder().add(jsonObjectBuilder).build();
    }
//        return Json.createArrayBuilder().add(addValues(defendant).add("addressAmended", isAddressAmended(defendant, ownParty).name().toLowerCase())).build();

    private JsonObjectBuilder addValues(TheirDetails defendant) {
        return new NullAwareJsonObjectBuilder()
            .add("type", defendant.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendant.getName())
            .add("address", addressMapper.map(defendant.getAddress()))
            .add("correspondenceAddress", defendant.getServiceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendant.getEmail().orElse(null))
            .add("businessName", extractOptionalFromSubclass(defendant, SoleTraderDetails.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendant, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendant, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber));
    }

    private YesNoOption isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        if (!ownParty.getAddress().equals(oppositeParty.getAddress())) {
            return YesNoOption.YES;
        }
        return YesNoOption.NO;
    }
}
