package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class DefendantJsonMapper {

    @Autowired
    private final AddressJsonMapper addressMapper;

    public DefendantJsonMapper(AddressJsonMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public JsonArray map(TheirDetails defendant) {
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("type", defendant.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendant.getName())
            .add("address", addressMapper.map(defendant.getAddress()))
            .add("correspondenceAddress", defendant.getServiceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendant.getEmail().orElse(null))
            .add("businessName", extractOptionalFromSubclass(defendant, SoleTraderDetails.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendant, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendant, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber));

        return Json.createArrayBuilder().add(jsonObjectBuilder).build();
    }

    public JsonArray map(Party defendantFromResponse, TheirDetails defendantFromClaim, String defendantsEmail) {

        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("type", defendantFromResponse.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendantFromResponse.getName())
            .add("address", addressMapper.map(defendantFromResponse.getAddress()))
            .add("correspondenceAddress", defendantFromResponse.getCorrespondenceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendantsEmail)
            .add("addressAmended", isAddressAmended(defendantFromResponse, defendantFromClaim).name().toLowerCase())
            .add("businessName", extractOptionalFromSubclass(defendantFromResponse, SoleTrader.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendantFromResponse, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendantFromResponse, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber));

        return Json.createArrayBuilder().add(jsonObjectBuilder).build();
    }

    private YesNoOption isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        if (!ownParty.getAddress().equals(oppositeParty.getAddress())) {
            return YesNoOption.YES;
        }
        return YesNoOption.NO;
    }
}
