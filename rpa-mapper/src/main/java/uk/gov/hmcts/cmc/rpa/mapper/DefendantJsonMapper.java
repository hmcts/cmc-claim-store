package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonArray;
import javax.json.stream.JsonCollectors;
import java.util.List;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
public class DefendantJsonMapper {

    @Autowired
    private final AddressJsonMapper mapAddress;

    public DefendantJsonMapper(AddressJsonMapper mapAddress) {
        this.mapAddress = mapAddress;
    }

    public JsonArray mapDefendants(List<TheirDetails> defendants) {
        return defendants.stream()
            .map(defendant -> new NullAwareJsonObjectBuilder()
                .add("type", defendant.getClass().getSimpleName().replace("Details", ""))
                .add("name", defendant.getName())
                .add("address", mapAddress.mapAddress(defendant.getAddress()))
                .add("correspondenceAddress", defendant.getServiceAddress().map(mapAddress::mapAddress).orElse(null))
                .add("emailAddress", defendant.getEmail().orElse(null))
                .add("businessName", extractOptionalFromSubclass(defendant, SoleTraderDetails.class, SoleTraderDetails::getBusinessName))
                .add("contactPerson", extractOptionalFromSubclass(defendant, HasContactPerson.class, HasContactPerson::getContactPerson))
                .add("companiesHouseNumber", extractOptionalFromSubclass(defendant, OrganisationDetails.class, OrganisationDetails::getCompaniesHouseNumber))
                .build())
            .collect(JsonCollectors.toJsonArray());
    }
}
