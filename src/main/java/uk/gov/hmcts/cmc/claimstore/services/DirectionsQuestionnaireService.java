package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGN_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.isPilotCourt;

@Service
public class DirectionsQuestionnaireService {

    public Optional<CaseEvent> prepareCaseEvent(ResponseRejection responseRejection) {
        DirectionsQuestionnaire directionsQuestionnaire = responseRejection.getDirectionsQuestionnaire()
            .orElseThrow(IllegalStateException::new);

        String courtName = directionsQuestionnaire.getHearingLocation().getCourtName();
        if (isOptedForMediation(responseRejection)) {
            return Optional.of(REFERRED_TO_MEDIATION);
        }

        if (isPilotCourt(courtName)) {
            return Optional.of(ASSIGN_FOR_DIRECTIONS);
        }

        return Optional.empty();
    }

}
