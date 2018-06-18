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
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        String defendantsEmail = claim.getDefendantEmail();
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", claim.getReferenceNumber())
            .add("defenceSubmittedOn", DateFormatter.format(claim.getRespondedAt()))
            .add("defenceResponse", extractFromSubclass(response, FullDefenceResponse.class, fullDefenceResponse -> fullDefenceResponse.getDefenceType().getDescription()))
            .add("defendants", defendantMapper.map(response.getDefendant(), claim.getClaimData().getDefendant(), defendantsEmail))
            .add("dateOfBirth", extractFromSubclass(response.getDefendant(),
                Individual.class, individual -> DateFormatter.format(individual.getDateOfBirth())))
            .add("phoneNumber", extractFromSubclass(response.getDefendant(),
                Party.class, party -> party.getMobilePhone().orElse(null)))
            .add("mediation", isMediationShown(response).name().toLowerCase())
            .build();
    }

    private YesNoOption isMediationShown(Response response) {
        if (FullDefenceResponse.class.isInstance(response)
            && ((FullDefenceResponse) response).getDefenceType().equals(DefenceType.DISPUTE)) {
            return response.getFreeMediation().orElse(YesNoOption.NO);
        }
        return YesNoOption.NO;
    }
}
