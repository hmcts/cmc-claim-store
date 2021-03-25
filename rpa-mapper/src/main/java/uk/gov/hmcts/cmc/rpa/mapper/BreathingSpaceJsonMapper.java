package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.HasContactPerson;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.helper.RPAMapperHelper;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class BreathingSpaceJsonMapper {


    public JsonObject map(Claim claim) {
        BreathingSpace breathingSpace = null;
        if (claim.getClaimData().getBreathingSpace().isPresent()) {
            breathingSpace = claim.getClaimData().getBreathingSpace().get();
            return new NullAwareJsonObjectBuilder()
                .add("caseNumber", claim.getReferenceNumber())
                .add("uniqueReferenceNumber", breathingSpace.getBsReferenceNumber() != null ?
                    breathingSpace.getBsReferenceNumber() : null)
                .add("breathingSpaceCode",
                    BreathingSpaceType.valueOf(breathingSpace.getBsType().name()).getValue().toString())
                .add("breathingSpaceEnteredDate", breathingSpace.getBsEnteredDate() != null ?
                    DateFormatter.format(breathingSpace.getBsEnteredDate()) : null)
                .add("breathingSpaceLiftedDate", breathingSpace.getBsLiftedDate() != null
                    ? DateFormatter.format(breathingSpace.getBsLiftedDate()) : null)
                .add("breathingSpaceEnteredDateByInsolvencyTeam", breathingSpace.getBsEnteredDateByInsolvencyTeam() != null ?
                    DateFormatter.format(breathingSpace.getBsEnteredDateByInsolvencyTeam()) : null)
                .add("breathingSpaceLiftedDateByInsolvencyTeam", breathingSpace.getBsLiftedDateByInsolvencyTeam() != null ?
                    DateFormatter.format(breathingSpace.getBsLiftedDateByInsolvencyTeam()) : null)
                .build();

        }
        return null;
    }

}
