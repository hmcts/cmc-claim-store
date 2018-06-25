package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
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
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonCollectors;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

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
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendant, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber))
            .build()).collect(JsonCollectors.toJsonArray());
    }

    public JsonArray map(Party defendantFromResponse, TheirDetails defendantFromClaim, String defendantsEmail) {
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder()
            .add("type", defendantFromResponse.getClass().getSimpleName().replace("Details", ""))
            .add("name", defendantFromResponse.getName())
            .add("address", addressMapper.map(defendantFromResponse.getAddress()))
            .add("correspondenceAddress", defendantFromResponse.getCorrespondenceAddress().map(addressMapper::map).orElse(null))
            .add("emailAddress", defendantsEmail)
            .add("addressAmended", isAddressAmended(defendantFromResponse, defendantFromClaim))
            .add("businessName", extractOptionalFromSubclass(defendantFromResponse, SoleTrader.class, value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
            .add("contactPerson", extractOptionalFromSubclass(defendantFromResponse, HasContactPerson.class, HasContactPerson::getContactPerson))
            .add("companiesHouseNumber", extractOptionalFromSubclass(defendantFromResponse, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber))
            .add("dateOfBirth", extractFromSubclass(defendantFromResponse, Individual.class, individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber", extractFromSubclass(defendantFromResponse, Party.class, party -> party.getMobilePhone().orElse(null)));

        return Json.createArrayBuilder().add(jsonObjectBuilder).build();
    }

    private boolean isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        return !ownParty.getAddress().equals(oppositeParty.getAddress()) ? true : false;
    }
}
