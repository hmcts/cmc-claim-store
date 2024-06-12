package uk.gov.hmcts.cmc.rpa.mapper;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonCollectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.List;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class SealedClaimJsonMapper {

    @Autowired
    private final DefendantJsonMapper defendantMapper;

    public SealedClaimJsonMapper(DefendantJsonMapper defendantJsonMapper) {
        this.defendantMapper = defendantJsonMapper;
    }

    public JsonObject map(Claim claim) {

        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()
                .orElseThrow(() -> new IllegalStateException("Missing issuedOn date"))))
            .add("serviceDate", DateFormatter.format(claim.getServiceDate()))
            .add("courtFee", claim.getClaimData().getFeesPaidInPounds().orElse(ZERO))
            .add("amountWithInterest", claim.getAmountWithInterestUntilIssueDate().orElse(null))
            .add("submitterEmail", claim.getSubmitterEmail())
            .add("helpWithFeesNumber", claim.getClaimData().getHelpWithFeesNumber().orElse(null))
            .add("claimants", mapClaimants(claim.getClaimData().getClaimants()))
            .add("defendants", defendantMapper.map(claim.getClaimData().getDefendants()))
            .build();
    }

    private JsonArray mapClaimants(List<Party> claimants) {
        return claimants.stream()
            .map(claimant -> new NullAwareJsonObjectBuilder()
                .add("type", claimant.getClass().getSimpleName())
                .add("name", claimant.getName())
                .add("address", mapAddress(claimant.getAddress()))
                .add("correspondenceAddress", claimant.getCorrespondenceAddress().map(this::mapAddress).orElse(null))
                .add("phoneNumber", claimant.getPhone().orElse(null))
                .add("dateOfBirth", extractFromSubclass(claimant, Individual.class,
                    individual -> DateFormatter.format(individual.getDateOfBirth())))
                .add("businessName", extractOptionalFromSubclass(claimant, SoleTrader.class,
                    value -> value.getBusinessName().map(RPAMapperHelper::prependWithTradingAs)))
                .add("contactPerson", extractOptionalFromSubclass(claimant, HasContactPerson.class,
                    HasContactPerson::getContactPerson))
                .build())
            .collect(JsonCollectors.toJsonArray());
    }

    private JsonObject mapAddress(Address address) {
        return new NullAwareJsonObjectBuilder()
            .add("line1", address.getLine1())
            .add("line2", address.getLine2())
            .add("line3", address.getLine3())
            .add("city", address.getCity())
            .add("postcode", address.getPostcode())
            .build();
    }

}
