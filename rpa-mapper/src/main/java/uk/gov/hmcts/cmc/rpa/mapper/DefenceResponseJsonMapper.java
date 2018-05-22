package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;

@Component
public class DefenceResponseJsonMapper {

    @Autowired
    private final AddressJsonMapper mapAddress;
    private final DefendantJsonMapper mapDefendant;

    public DefenceResponseJsonMapper(AddressJsonMapper mapAddress, DefendantJsonMapper mapDefendant) {
        this.mapAddress = mapAddress;
        this.mapDefendant = mapDefendant;
    }

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("defendants", mapDefendant.mapDefendants(claim.getClaimData().getDefendants()))
            .add("dateOfBirth",
                        extractFromSubclass(claim.getClaimData().getDefendant(),
                            Individual.class,
                            individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber",
                        extractFromSubclass(
                            claim.getClaimData().getDefendant(),
                            Party.class,
                            party -> party.getMobilePhone().orElse(null)))
            .build();
    }

}
