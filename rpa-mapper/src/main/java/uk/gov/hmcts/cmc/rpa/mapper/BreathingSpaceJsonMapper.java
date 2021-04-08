package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
@SuppressWarnings({"LineLength"})
public class BreathingSpaceJsonMapper {

    public JsonObject map(Claim claim) {
        BreathingSpace breathingSpace = null;
        if (claim.getClaimData().getBreathingSpace().isPresent()) {
            breathingSpace = claim.getClaimData().getBreathingSpace().get();
            return new NullAwareJsonObjectBuilder()
                .add("caseNumber", claim.getReferenceNumber())
                .add("uniqueReferenceNumber", breathingSpace.getBsReferenceNumber() != null
                    ? breathingSpace.getBsReferenceNumber() : null)
                .add("breathingSpaceCode",
                    BreathingSpaceType.valueOf(breathingSpace.getBsType().name()).getValue().toString())
                .add("breathingSpaceEnteredDate", breathingSpace.getBsEnteredDate() != null
                    ? DateFormatter.format(breathingSpace.getBsEnteredDate()) : null)
                .add("breathingSpaceLiftedDate", breathingSpace.getBsLiftedDate() != null
                    ? DateFormatter.format(breathingSpace.getBsLiftedDate()) : null)
                .add("breathingSpaceEnteredDateByInsolvencyTeam", breathingSpace.getBsEnteredDateByInsolvencyTeam() != null
                    ? DateFormatter.format(breathingSpace.getBsEnteredDateByInsolvencyTeam())
                    : DateFormatter.format(breathingSpace.getBsEnteredDate()))
                .add("breathingSpaceLiftedDateByInsolvencyTeam", breathingSpace.getBsLiftedDateByInsolvencyTeam() != null
                    ? DateFormatter.format(breathingSpace.getBsLiftedDateByInsolvencyTeam())
                    : (breathingSpace.getBsLiftedFlag().equals("Yes") ? DateFormatter.format(breathingSpace.getBsLiftedDate()) : null))
                .build();
        }
        return null;
    }
}
