package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import java.util.List;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class SealedClaimJsonMapper {

    private final AddressJsonMapper mapAddress;
    private final DefendantJsonMapper mapDefendant;

    @Autowired
    public SealedClaimJsonMapper(AddressJsonMapper mapAddress, DefendantJsonMapper mapDefendant) {
        this.mapAddress = mapAddress;
        this.mapDefendant = mapDefendant;
    }

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("serviceDate", DateFormatter.format(claim.getServiceDate()))
            .add("courtFee", claim.getClaimData().getFeesPaidInPound())
            .add("amountWithInterest", claim.getTotalAmountTillToday().orElse(null))
            .add("submitterEmail", claim.getSubmitterEmail())
            .add("claimants", mapClaimants(claim.getClaimData().getClaimants()))
            .add("defendants", mapDefendant.mapDefendants(claim.getClaimData().getDefendants()))
            .build();
    }

    private JsonArray mapClaimants(List<Party> claimants) {
        return claimants.stream()
            .map(claimant -> new NullAwareJsonObjectBuilder()
                .add("type", claimant.getClass().getSimpleName())
                .add("name", claimant.getName())
                .add("address", mapAddress.mapAddress(claimant.getAddress()))
                .add("correspondenceAddress", claimant.getCorrespondenceAddress().map(mapAddress::mapAddress).orElse(null))
                .add("phoneNumber", claimant.getMobilePhone().orElse(null))
                .add("dateOfBirth", extractFromSubclass(claimant, Individual.class, individual -> DateFormatter.format(individual.getDateOfBirth())))
                .add("businessName", extractOptionalFromSubclass(claimant, SoleTrader.class, SoleTrader::getBusinessName))
                .add("contactPerson", extractOptionalFromSubclass(claimant, HasContactPerson.class, HasContactPerson::getContactPerson))
                .add("companiesHouseNumber", extractOptionalFromSubclass(claimant, Organisation.class, Organisation::getCompaniesHouseNumber))
                .build())
            .collect(JsonCollectors.toJsonArray());
    }

}
