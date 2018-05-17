package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
public class DefenceResponseJsonMapper {

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("defendantsName", claim.getClaimData().getDefendant().getName())
            .add("defendantContactPerson", extractOptionalFromSubclass(claim.getClaimData().getDefendant(),
                HasContactPerson.class,
                HasContactPerson::getContactPerson))
            .add("defendantsAddress", mapAddress(claim.getClaimData().getDefendant().getAddress()))
            .add("defendantsCorrespondenceAddress", claim.getClaimData()
                .getDefendant()
                .getServiceAddress()
                .map(this::mapAddress)
                .orElse(null))
            .add("defendantsEmail", claim.getClaimData().getDefendant().getEmail().orElse(null))
            .add("dateOfBirth", extractFromSubclass(claim.getClaimData().getDefendant(),
                Individual.class,
                individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber", extractFromSubclass(claim.getClaimData().getDefendant(),
                Party.class,
                party -> party.getMobilePhone().orElse(null)))
            .build();
    }

    private JsonObject mapAddress(Address address) {
        return createObjectBuilder()
            .add("line1", address.getLine1())
            .add("line2", address.getLine2())
            .add("line3", address.getLine3())
            .add("city", address.getCity())
            .add("postcode", address.getPostcode())
            .build();
    }

}
