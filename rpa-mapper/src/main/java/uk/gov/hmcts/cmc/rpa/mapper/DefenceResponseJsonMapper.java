package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

import java.util.Optional;

import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class DefenceResponseJsonMapper {

    @Autowired
    private final DefendantJsonMapper defendantMapper;

    public DefenceResponseJsonMapper(DefendantJsonMapper defendantMapper) {
        this.defendantMapper = defendantMapper;
    }

    public JsonObject map(Claim claim) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("issueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("defenceResponse", extractFromSubclass(claim.getResponse(),
                FullDefenceResponse.class,fullDefenceResponse -> fullDefenceResponse.getDefenceType().getDescription()))
            .add("defendants", defendantMapper.mapDefendants(claim.getClaimData().getDefendants()))
            .add("dateOfBirth", extractFromSubclass(claim.getClaimData().getDefendant(),
                Individual.class, individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber", extractFromSubclass(claim.getClaimData().getDefendant(),
                Party.class, party -> party.getMobilePhone().orElse(null)))
            .add("mediation", isMediationShown(claim.getResponse()))
            .build();
    }

    private String isMediationShown(Optional<Response> response){
        if(response.isPresent()){
            if(response.get() instanceof FullDefenceResponse){
                FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) response.get();
                if(fullDefenceResponse.getDefenceType().equals(DefenceType.DISPUTE)){
                    return "yes";
                }
            }
        }
        return "no";
    }

}
