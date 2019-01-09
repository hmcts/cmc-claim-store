package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Claim;
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
            .add("responseSubmittedOn", DateFormatter.format(claim.getRespondedAt()))
            .add("defenceResponse", defenceResponse(response))
            .add("defendant", defendantMapper.map(response.getDefendant(), claim.getClaimData().getDefendant(), defendantsEmail))
            .add("mediation", isMediationSelected(response))
            .build();
    }

    private String defenceResponse(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                return extractFromSubclass(response, FullDefenceResponse.class,
                    fullDefenceResponse -> fullDefenceResponse.getDefenceType().name());
            case FULL_ADMISSION:
            case PART_ADMISSION:
                return response.getResponseType().name();
            default:
                throw new IllegalArgumentException("Invalid response type: " + response.getResponseType());
        }
    }

    private boolean isMediationSelected(Response response) {
        if (FullDefenceResponse.class.isInstance(response)
            && ((FullDefenceResponse) response).getDefenceType().equals(DefenceType.DISPUTE)) {
            YesNoOption yesNoOption = response.getFreeMediation().orElse(YesNoOption.NO);
            if (yesNoOption.equals(YesNoOption.YES)) {
                return true;
            }
        }
        return false;
    }
}
